/**
 *  Media Scene
 *
 *  Copyright 2018 Jake Tebbett
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
 * VERSION CONTROL - Media Scene Child
 * ###############
 *
 *  v1.0 - Initial Release
 *  v1.1  - Removed Routines
 */

definition(
    name: "MediaSceneChild",
    namespace: "jebbett",
    author: "Jake Tebbett",
    description: "Control scenes based on media state and type",
    category: "My Apps",
    parent: "jebbett:MediaScene",
    iconUrl: "https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png",
    iconX2Url: "https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png",
    iconX3Url: "https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png"
)


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
  	state.catcherRunning = false
    subscribe(playerDT, "status", PlayerDTCommandRecieved)
}

preferences {
    page name: "childPage"
    //Child Pages
    page name: "pageDoThis"
    page name: "pageWhenThis"
    page name: "pageMediaSettings"
}


def childPage() {
    dynamicPage(name: "childPage", uninstall: true, install: true) {
        section() {
                label title: "<b>Room Name</b>", defaultValue: app.label, required: false
        }
        section ("<b>For This Device</b>"){

            input(name: "playerDT", type: "capability.musicPlayer", title: "Media Player Device", multiple: false, required:false)      
      	}
        section("<b>Lights</b>") {
			input "dimmers1", "capability.switchLevel", title: "Adjust level of these bulbs", multiple: true, required: false, submitOnChange: true
            input "hues1", "capability.colorControl", title: "Adjust level and color of these bulbs", multiple:true, required:false, submitOnChange: true
            if(hues1||dimmers1) {
            input(name: "iLevelOnPlay1", type: "number", title: "Level on Play", defaultValue:0)
            input(name: "iLevelOnPause1", type: "number", title: "Level on Pause", defaultValue:30)
            input(name: "iLevelOnStop1", type: "number", title: "Level on Stop", defaultValue:100)
            }
            if(hues1) {
				input "colorOnPlay", "enum", title: "Hue Bulbs > Color On Play", required: false, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink"]
                input "colorOnPause", "enum", title: "Hue Bulbs > Color On Pause", required: false, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink"]
                input "colorOnStop", "enum", title: "Hue Bulbs > Color On Stop", required: false, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink"]
                input(name: "tempOnPlay", description: "1000..9999", type: "number", range: "1000..9999", title: "Color Temperature on Play (°K)", required: false)
                input(name: "tempOnPause", description: "1000..9999", type: "number", range: "1000..9999", title: "Color Temperature on Pause (°K)", required: false)
                input(name: "tempOnStop", description: "1000..9999", type: "number", range: "1000..9999", title: "Color Temperature on Stop (°K)", required: false)
            }
            input(name: "bDimOnlyIfOn1", type: "bool", title: "Dim bulbs only if they're already on", required: false)
        }
		section("<b>Switches</b>") {
        	input "switches2", "capability.switch", title:"Switches On when Playing", multiple: true, required: false
            input "switches1", "capability.switch", title:"Switches Off when Playing", multiple: true, required: false
            input(name: "bReturnState1", type: "bool", title: "Switches return to original state when Stopped", required: false)
            input(name: "bSwitchOffOnPause1", type: "bool", title: "Switches use Play config when Paused", required: false)
            input(name: "switchOnPlay", type: "bool", title: "Switches only change on 'Play'", required: false)
            paragraph "The below switches do not toggle off when state becomes inactive, ideal for tiggering external App scenes"
            input "mSwitchPlay", "capability.switch", title:"Momentary switch on Play", multiple: true, required: false
            input "mSwitchPause", "capability.switch", title:"Momentary switch on Pause", multiple: true, required: false
            input "mSwitchStop", "capability.switch", title:"Momentary switch on Stop", multiple: true, required: false
            
        }
		section("<b>Modes</b>") {
			input "playMode1", "mode", title: "Mode when playing", required:false
			input "pauseMode1", "mode", title: "Mode when paused", required:false
			input "stopMode1", "mode", title: "Mode when stopped", required:false
		}
        section("<b>Media Settings</b>") {	
			input(name: "bTreatTrailersAsPause1", type: "bool", title: "Use pause config for movie trailers", required: false)
            input(name: "stopDelay", type: "number", title: "Delay stop action", required:false, defaultValue:0)
            input(name: "pauseDelay", type: "number", title: "Delay pause action", required:false, defaultValue:0)
		}
        section("<b>Restrictions</b>") {
			input "mediaTypeOk", "enum", title: "Only for media types:", multiple: true, submitOnChange: true, required: false,
			options: ['movie', 'episode', 'clip', 'track']
        	input "disabled", "capability.switch", title: "Switch to disable when On", required: false, multiple: false
            input "activeMode", "mode", title: "Only run in selected modes", multiple: true, required:false
        }
    }
}


// Recieve command from MusicPlayer device type
def PlayerDTCommandRecieved(evt){
	def mediaType = playerDT.currentplaybackType ?: null
	if(evt.value=="playing"){		AppCommandRecieved("onplay", mediaType)}
	else if(evt.value=="stopped"){	AppCommandRecieved("onstop", mediaType)}
    else if(evt.value=="paused"){	AppCommandRecieved("onpause", mediaType)}
}



def AppCommandRecieved(command, mediaType) {

// Stop running if disable switch is activated    
    if (disabled != null) {if(disabled.currentSwitch == "on") {logWriter ("Disabled via switch"); return}}
    if (activeMode != null && !activeMode.contains(location.mode)) {logWriter ("Disabled via invalid mode"); return}

// Check if Media Type is correct
	if(mediaTypeOk){
		def mediaTypeFound = mediaTypeOk.find { item -> item == mediaType}
    	if(mediaTypeFound == null) {logWriter ("Match NOT found for media type: ${mediaType}"); return}
	}
    
//Translate play to pause if bTreatTrailersAsPause is enabled for this room
    if(settings?.bTreatTrailersAsPause1 && mediaType == "clip" && command == "onplay") {command = "onpause"}

// Unschedule delays
	unschedule(StopCommand)
    unschedule(PauseCommand)

// Play, Pause or Stop
    if (command == "onplay") {
    	logWriter ("Playing")
        PlayCommand()
    }
    else if (command == "onpause") {        
        logWriter ("Paused")
        if(!settings?.pauseDelay || pauseDelay == "0"){
        	PauseCommand()
        }else{
            logWriter ("Pause Action Delay")
        	runIn(settings?.pauseDelay.value, PauseCommand)
    	}
    }
    else if (command == "onstop") {
        logWriter ("Stopped")
        if(!settings?.stopDelay || stopDelay == "0"){
        	StopCommand()
        }else{
           	logWriter ("Stop Action Delay")
        	runIn(settings?.stopDelay.value, StopCommand)
        }
    }
}

def PlayCommand(){
	if(!state.catcherRunning){
        catchState("switches1")
    	catchState("switches2")
        state.catcherRunning = true
    }
    if(settings?.playMode1){setLocationMode(playMode1)}
	SetLevels(iLevelOnPlay1, colorOnPlay, tempOnPlay)
    SetSwitchesOff()
    mSwitchPlay?.on()
}

def PauseCommand(){
    if(settings?.pauseMode1){setLocationMode(pauseMode1)}
   	SetLevels(iLevelOnPause1, colorOnPause, tempOnPause)
    mSwitchPause?.on()
    if(settings?.bSwitchOffOnPause1) {
   		SetSwitchesOff()
    } else {
       	if(state.catcherRunning && settings?.bReturnState1){
       		returnToState("switches1")
   			returnToState("switches2")
           	state.catcherRunning = false
       	}else{
       		SetSwitchesOn()
           	state.catcherRunning = false
       	}
    }
}

//Stop command
def StopCommand(){

	if(settings?.stopMode1){setLocationMode(settings?.stopMode1)}
    SetLevels(iLevelOnStop1, colorOnStop, tempOnStop)
    mSwitchStop?.on()
    if(state.catcherRunning && settings?.bReturnState1){
       	returnToState("switches1")
    	returnToState("switches2")
        state.catcherRunning = false
    }else{
       	SetSwitchesOn()
        state.catcherRunning = false
    }
}

// Actions
def SetSwitchesOn() {
	if(!switchOnPlay){
		switches1?.on()
    	switches2?.off()
    }
}
def SetSwitchesOff() {
	switches1?.off()
    switches2?.on()
}

def SetLevels(level, acolor, temp) {
	// If color specified set hues
    if (level != null) {
    	def hueColor = 23
		def saturation = 56
		switch(acolor) {
			case "White":
				hueColor = 52
				saturation = 19
				break;
			case "Daylight":
				hueColor = 53
				saturation = 91
				break;
			case "Soft White":
				hueColor = 23
				saturation = 56
				break;
			case "Warm White":
				hueColor = 20
				saturation = 80 //83
				break;
			case "Blue":
				hueColor = 70
				break;
			case "Green":
				hueColor = 35
				break;
			case "Yellow":
				hueColor = 25
				break;
			case "Orange":
				hueColor = 10
				break;
			case "Purple":
				hueColor = 75
				break;
			case "Pink":
				hueColor = 83
				break;
			case "Red":
				hueColor = 100
				break;
		}
        
        if (settings?.bDimOnlyIfOn1){
        	if(acolor != null){ 	hues1?.each 	{ hue -> if ("on" == hue.currentSwitch) 	{ hue.setColor([hue: hueColor, saturation: saturation, level: level]) } } }
            else if(temp != null){ 	hues1?.each 	{ hue -> if ("on" == hue.currentSwitch) 	{ hue.setColorTemperature(temp) } } }
            else {					hues1?.each 	{ hue -> if ("on" == hue.currentSwitch) 	{ hue.setLevel(level) } } }           
        							dimmers1?.each 	{ bulb -> if ("on" == bulb.currentSwitch) 	{ bulb.setLevel(level) } }
        }else{
        	// color takes priority over temperature, dimmers will still set temperature if available
        	if(acolor != null){		hues1?.setColor([hue: hueColor, saturation: saturation, level: level]) }
            else if(temp != null){	hues1?.setColorTemperature(temp) }
			dimmers1?.setLevel(level)
        }
	}
}

//Save state
private catchState(switches) {
        settings."${switches}"?.each { switcher -> state."${switcher.id}State" = switcher.currentValue("switch")
        	logWriter (switcher.currentValue("switch"))
        }
}
//Return to state
private returnToState(switches) {
	settings."${switches}"?.each {switcher -> 
    	if(state."${switcher.id}State" == "on") {switcher.on()}
        if(state."${switcher.id}State" == "off") {switcher.off()}
    }
}


//// GENERIC CODE

private def logWriter(value) {
    if(parent.debugLogging) {log.debug "Media Scene [${app.label}] >> ${value}"}
}
