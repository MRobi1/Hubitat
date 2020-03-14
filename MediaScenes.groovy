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
 * VERSION CONTROL - Media Scene
 * ###############
 *
 *  v1.0 - Initial Release
 *
 */

definition(
    name: "MediaScene",
    namespace: "jebbett",
    author: "Jake Tebbett",
    description: "Control scenes based on media state and type",
    category: "My Apps",
    iconUrl: "https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png",
    iconX2Url: "https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png",
    iconX3Url: "https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png"
)

def installed() {
	state.installedOK = true
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
}

preferences {
    page name: "installedCheck"
	page name: "installPage"
	page name: "mainMenu"
}

def installedCheck(){
	if (state?.installedOK){
		mainMenu()
	}else{
		installPage()
	}
}

def installPage() {
    dynamicPage(name: "installPage", title: "Rooms", install: true, uninstall: false, submitOnChange: true) {              
        section(""){
            paragraph "<h1><img src='https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png' width='64' height='64' />&nbsp;<strong><span style='color: #ff6600;'>MediaScene</span></strong></h1>"
			paragraph "Press 'Done' to install..."
        }
    }
}

def mainMenu() {
    dynamicPage(name: "mainMenu", install: true, uninstall: true, submitOnChange: true) {              
        section(""){
            paragraph "<h1><img src='https://github.com/jebbett/MediaScene/raw/master/Icons/MediaScene.png' width='64' height='64' />&nbsp;<strong><span style='color: #ff6600;'>MediaScene</span></strong></h1>"
			paragraph "<b>MediaScene allows you to control devices based on the playback state of a media device</b>"
			paragraph "This can be used with devices created by Plex Communicator or other media devices"
        }
		section {
            app(name: "childapp", appName: "MediaSceneChild", namespace: "jebbett", title: "Create New Room", multiple: true)
    	}
        section {
        	input(name: "debugLogging", type: "bool", title: "Enable Debugging", required: false)
        }
    }
}
