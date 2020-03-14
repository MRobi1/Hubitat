/**
 *  Emby Communicator Device
 *
 *  Copyright 2020 Mike Robichaud (MRobi)
 *	Credit To: Jake Tebbett (jebbett) for his Plex Communicator code used as a base
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
 * 2020-03-01	1.0		Test Build		
 */

metadata {
	definition (name: "Emby Communicator Device", namespace: "MRobi", author: "MRobi", importUrl: "https://raw.githubusercontent.com/MRobi1/Hubitat/master/EmbyCommunicatorDevice.groovy") {
	capability "Music Player"
    command "playbackType", ["string"]
	attribute "playbackType", "string"
    attribute "playbackTitle", "string"
	}
}

// External
def playbackType(type) {
	sendEvent(name: "playbackType", value: type);
    log.debug "Playback type set as $type"
}

def setPlayStatus(type){
    // Value needs to be playing, paused or stopped
    sendEvent(name: "status", value: "$type")
	log.debug "Status set to $type"
}

def playbackTitle(title) {
sendEvent(name: "playbackTitle", value: title);
log.debug "Title set to $title"
}

def play() {	        
    sendEvent(name: "status", value: "playing");
}

def pause() {
    sendEvent(name: "status", value: "paused");
}

def stop() {
    sendEvent(name: "status", value: "stopped");    
}
