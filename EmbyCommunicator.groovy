import groovy.json.JsonSlurper
/**
 *  Emby Communicator
 *
 *  Copyright 2020 Mike Robichaud
 *	Credit To: Jake Tebbett for his Plex Communicator code used as a base, Keo, Christian Hjelseth, iBeech & Ph4r as snippets of code taken and amended from their apps
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * 
 * VERSION CONTROL
 * ###############
 *
 *  v1.0 - Test Release
 *
 */

definition(
    name: "Emby Communicator",
    namespace: "MRobi",
    author: "Mike Robichaud",
    importUrl: "https://raw.githubusercontent.com/MRobi1/Hubitat/master/EmbyCommunicator.groovy",
    description: "Allow Hubitat and Emby to Communicate",
    category: "My Apps",
    iconUrl: "https://github.com/MRobi1/Hubitat/raw/master/icon.png",
    iconX2Url: "https://github.com/MRobi1/Hubitat/raw/master/icon.png",
    iconX3Url: "https://github.com/MRobi1/Hubitat/raw/master/icon.png",
    oauth: [displayName: "EmbyServer", displayLink: ""])


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
	// sub to Emby now playing response
    subscribe(location, null, response, [filterEvents:false])
    // Add New Devices
    def storedDevices = state.embyClients
    settings.devices.each {deviceId ->
        try {
            def existingDevice = getChildDevice(deviceId)
            if(!existingDevice) {
                def theHub = location.hubs[0]
            	log.warn "${deviceId} and ${theHub}"
                //def childDevice = addChildDevice("MRobi", "Emby Communicator Device", deviceId, theHub.id, [name: "${deviceId}", isComponent: false])
                def childDevice = addChildDevice("MRobi", "Emby Communicator Device", deviceId, theHub.id, [name: deviceId, label: storedDevices."$deviceId".name, completedSetup: false])
            }
        } catch (e) { log.error "Error creating device: ${e}" }
    }
    // Clean up child devices
    if(settings?.devices) {
    	getChildDevices().each { if(settings.devices.contains(it.deviceNetworkId)){}else{deleteChildDevice("${it.deviceNetworkId}")} }
    }else{
    	getChildDevices().each { deleteChildDevice("${it.deviceNetworkId}") }
    }
}

preferences {
	page name: "mainMenu"
    page name: "noAuthPage"
    page name: "authPage"
    page name: "clientPage"
    page name: "clearClients"
    page name: "mainPage"
    page name: "WebhooksSettings"
}

mappings {
  path("/ewhset") 					{ action: [GET: "ewhset"]   }
  path("/ewh") 						{ action: [POST: "embyWebHookHandler"] }
}


/***********************************************************
** Main Pages
************************************************************/

def mainMenu() {
	// Get HE Token
    try { if (!state.accessToken) {createAccessToken()} }
    catch (Exception e) {
    	log.info "Unable to create access token, OAuth has probably not been enabled in IDE: $e"
        return noAuthPage()
    }
	return mainPage()
}

def noAuthPage() {
	
	return dynamicPage(name: "noAuthPage", uninstall: true, install: true) {
		section("*Error* You have not enabled OAuth when installing the app code, please enable OAuth")
    }
}

def mainPage() {
	return dynamicPage(name: "mainPage", uninstall: true, install: true) {
        section(""){
            paragraph "<h1><img src='https://github.com/MRobi1/Hubitat/raw/master/icon.png' width='64' height='64' />&nbsp;<strong><span style='color: #000000;'>emby</span></strong></h1>"
			paragraph "<b>Emby Communicator creates virtual devices in Hubitat to allow for advanced automations based on Emby play states</b>"
        }
        section("Main Menu") {
        	href "authPage", title:"Emby Account Details", description: "Enter your IP, Port and API Key"
			href "clientPage", title:"Select Your Devices", description: "Select the devices you want to monitor" 
            href(name: "WebhooksSettings", title: "Webhook Settings", required: false, page: "WebhooksSettings", description: "Retrieve your webhook address")
    	}
        section("If you want to control lighting scenes then the 'MediaScene' SmartApp is ideal for this purpose"){}
        section("This app is developed by MRobi, additional credit goes to jebbett (PlexCommunicator), Keo (Emby to Vudu), to Christian H (Plex2SmartThings), iBeech (Plex Home Theatre) & Ph4r (Improved Plex control)."){}
    }
}

def WebhooksSettings() {
    dynamicPage(name: "WebhooksSettings", install: false, uninstall: false) {      
        section(""){
            paragraph "<h1><img src='https://github.com/MRobi1/Hubitat/raw/master/icon.png' width='64' height='64' />&nbsp;<strong><span style='color: #000000;'>emby</span></strong></h1>"
			paragraph "<b>Click the link below to retrieve your webhook address. This address must be copied into your webhooks settings on your Emby server.</b>"
            paragraph "Note: Your Emby Server will need to be on version 4.3.1.0 or above OR to have the 3rd party webhooks plugin installed"
        	href url: "${getLocalApiServerUrl()}/${app.id}/ewhset?access_token=${state.accessToken}", style:"embedded", required:false, title:"Emby Webhooks Settings", description: ""  		
        }
		section("NOTE: The address in the link above has also been sent to Live Logging, for easy access from a computer."){}
        	log.debug(
        		"\n ## URL FOR USE IN EMBY WEBHOOKS ##\n${getFullLocalApiServerUrl()}/ewh?access_token=${state.accessToken}"+
        		"<!ENTITY accessToken '${state.accessToken}'>\n"+
				"<!ENTITY appId '${app.id}'>\n")
	}
}

def ewhset() {
    def html = """<html><head><title>Emby2Hubitat Settings</title></head><body><h1>
        ${getFullLocalApiServerUrl()}/ewh?access_token=${state.accessToken}<br />
    </h1></body></html>"""
    render contentType: "text/html", data: html, status: 200
}


/***********************************************************
** Emby Authentication
************************************************************/

def authPage() {
    return dynamicPage(name: "authPage", install: false) {
        def hub = location.hubs[0]
       section(""){
            paragraph "<h1><img src='https://github.com/MRobi1/Hubitat/raw/master/icon.png' width='64' height='64' />&nbsp;<strong><span style='color: #000000;'>emby</span></strong></h1>"
			paragraph "<b>Please enter the IP address for your Emby server, port (default 8096) and your API Key which can be created in the API Keys section of your Emby server.</b>"
        }
        section("Emby Server Details") {
            input "embyServerIP", "text", "title": "Server IP", multiple: false, required: true
			input "embyServerPort", "text", "title": "Server Port", multiple: false, required: true, defaultValue: "8096"
			input("embyApiKey", "text", title: "API Key", description: "Your Emby API Key", required: true)
		}
    }
}

/***********************************************************
** CLIENTS
************************************************************/


def clientPage() {
    getClients()
    def devs = getClientList()
	return dynamicPage(name: "clientPage", uninstall: false, install: false) {
        section(""){
            paragraph "<h1><img src='https://github.com/MRobi1/Hubitat/raw/master/icon.png' width='64' height='64' />&nbsp;<strong><span style='color: #000000;'>emby</span></strong></h1>"
			paragraph "<b>Please select all devices you would like to monitor. A virtual device with the device name will be created in Hubitat.</b>"
        	input "devices", "enum", title: "Select Your Devices", options: devs, multiple: true, required: false, submitOnChange: true
  		}
        if(!devices){
            section("*CAUTION*"){
            	href(name: "clearClients", title:"RESET Devices List", description: "", page: "clearClients", required: false)
            }
        }else{
        section("To Reset Devices List - Untick all devices in the list above, and the option to reset will appear"){}
        }
    }
}


def clearClients() {
	state.embyClients = [:]
    mainPage()
}

def getClientList() {
    def devList = [:]
    state.embyClients.each { id, details -> devList << [ "$id": "${details.name}" ] }
//    state.playingClients.each { id, details -> devList << [ "$id": "${details.name} [►]" ] }
    return devList.sort { a, b -> a.value.toLowerCase() <=> b.value.toLowerCase() }
}

def getClients(){
    // set lists
	def isMap = state.embyClients instanceof Map
    if(!isMap){state.embyClients = [:]}
    // Get devices.json clients
    getClientsJSON()
	executeRequest("http://${settings.embyServerIP}:${settings.embyServerPort}/emby/Devices?&api_key=${settings.embyApiKey}", "GET")
}

def executeRequest(Path, method) {    
	def headers = [:] 
	headers.put("HOST", "$settings.embyServerIP:${settings.embyServerPort}")
    headers.put("X-Emby-Token", state.authenticationToken)	
	try {    
            def actualAction = new hubitat.device.HubAction( 
		    method: method,
		    path: Path,
		    headers: headers)
		sendHubCommand(actualAction)   
	}
	catch (Exception e) {log.debug "Hit Exception $e on $hubAction"}
}

def getClientsJSON() {
    def jsonDevices = [:]
    def paramsg = [
		uri: "http://${settings.embyServerIP}:${settings.embyServerPort}/emby/Devices?&api_key=${settings.embyApiKey}",
        contentType: 'application/json',
	]
	httpGet(paramsg) { resp ->
        log.debug "Parsing Emby Devices"
        def devices = resp.data.Items
        devices.each { thing ->        
        	// If not these things
        	if(thing.Name !="Emby Communicator"){      	
            	//Define name based on name unless blank then use device name
                def whatToCallMe = "Unknown"
                if(thing.Name != "") 		{whatToCallMe = "${thing.Name}-${thing.AppName}"}
                else if(thing.Name!="")	{whatToCallMe = "${thing.AppName}"}  
                jsonDevices << [ (thing.Id): [name: "${whatToCallMe}", id: "${thing.Id}"]]
        	}
    	}   
    }
    //Get from status
    state.embyClients << jsonDevices
}

/***********************************************************
** INPUT HANDLERS
************************************************************/

def embyWebHookHandler(){
    def payloadStart = request.body.indexOf('application/json') + 78
    def newBody = request.body.substring(payloadStart)
    //log.debug "Webhook Received with payload - $newBody"
	def jsonSlurper = new JsonSlurper()
   	def embyJSON = jsonSlurper.parseText(newBody)
    //log.debug "Event JSON: ${embyJSON.Event}"
	def playerID = embyJSON.Session.DeviceId
    def userName = embyJSON.User.Name
	def mediaType = embyJSON.Item.Type
    def status = embyJSON.Event
    def mediaTitle = embyJSON.Item.Name
    def seriesName = embyJSON.Item.SeriesName
    def embyEvent = [:] << [ id: "$playerID", type: "$mediaType", series: "$seriesName", title: "$mediaTitle", status: "$status", user: "$userName" ]
    //log.debug embyEvent
    eventHandler(embyEvent)
}

/***********************************************************
** DTH OUTPUT
************************************************************/

def eventHandler(event) {
    def status = event.status as String
    // change command to right format
    switch(status) {
		case ["media.play","media.resume","media.scrobble","onplay","play","playback.start","playback.unpause"]:	status = "playing"; break;
        case ["media.pause","onpause","pause","playback.pause"]:									status = "paused"; 	break;
        case ["media.stop","onstop","stop","playback.stop","playback.scrobble"]:									status = "stopped"; break;
    }
    //log.debug "Playback Status: $status"
    getChildDevices().each { pcd ->
        if (event.id == pcd.deviceNetworkId){
        	pcd.setPlayStatus(status)
            pcd.playbackUser(event.user)
            pcd.playbackType(event.type)
            pcd.playbackTitle(event.title)
            pcd.playbackSeries(event.series)
        }
    }
} 
