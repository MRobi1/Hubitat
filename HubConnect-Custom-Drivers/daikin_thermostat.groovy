/*
 *	Copyright 2019-2020 Steve White, Retail Media Concepts LLC.
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *	use this file except in compliance with the License. You may obtain a copy
 *	of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *	License for the specific language governing permissions and limitations
 *	under the License.
 *
 */
def getDriverVersion() {[platform: "Universal", major: 2, minor: 0, build: 0]}

metadata
{
	definition(name: "HubConnect Daikin", namespace: "shackrat", author: "Steve White & Mike Robichaud")
	{
	capability "Thermostat"
        capability "Temperature Measurement"
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
        capability "Refresh"
        capability "Polling"

        attribute "outsideTemp", "number"
        attribute "targetTemp", "number"
        attribute "currMode", "string"
        attribute "fanAPISupport", "string"
        attribute "fanRate", "string"
        attribute "fanDirection", "string"
        attribute "statusText", "string"
        attribute "connection", "string"
	attribute "version", "string"

        command "fan"
        command "dry"
        command "tempUp"
        command "tempDown"
        command "fanRateAuto"
        command "fanRateSilence"
        command "fanDirectionVertical"
        command "fanDirectionHorizontal"
        command "setFanRate", ["number"]
        command "setTemperature", ["number"]
	command "sync"
	}
}


/*
	installed
*/
def installed()
{
	initialize()
}


/*
	updated
*/
def updated()
{
	initialize()
}


/*
	initialize
*/
def initialize()
{
	refresh()
}


/*
	uninstalled

	Reports to the remote that this device is being uninstalled.
*/
def uninstalled()
{
	// Report
	parent?.sendDeviceEvent(device.deviceNetworkId, "uninstalled")
}


/*
	refresh
*/
def refresh()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "refresh")
}


/*
	sync
*/
def sync()
{
	// The server will respond with updated status and details
	parent.syncDevice(device.deviceNetworkId, "Thermostat")
	sendEvent([name: "version", value: "v${driverVersion.major}.${driverVersion.minor}.${driverVersion.build}"])
}
