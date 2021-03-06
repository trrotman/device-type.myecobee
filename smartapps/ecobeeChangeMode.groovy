/**
 *  ecobeeChangeMode
 *
 *  Copyright 2014 Yves Racine
 *  linkedIn profile: ca.linkedin.com/pub/yves-racine-m-sc-a/0/406/4b/
 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * Change the mode manually (by pressing the app's play button) and automatically at the ecobee thermostat(s)
 * If you need to set it for both Away and Home modes, you'd need to save them as 2 distinct apps
 * Don't forget to set the app to run only for the target mode.
 */
definition(
	name: "ecobeeChangeMode",
	namespace: "yracine",
	author: "Yves Racine",
	description:
	"Change the mode manually (by pressing the app's play button) and automatically at the ecobee thermostat(s)",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png"
)

preferences {
	page(name: "selectThermostats", title: "Thermostats", install: false , uninstall: true, nextPage: "selectProgram") {
		section("About") {
			paragraph "ecobeeChangeMode, the smartapp that sets your ecobee thermostat to a given program/climate ['Away', 'Home', 'Night']" + 
                		" based on ST hello mode."
			paragraph "Version 1.9\n\n" +
				"If you like this app, please support the developer via PayPal:\n\nyracine@yahoo.com\n\n" +
				"Copyright©2014 Yves Racine"
			href url: "http://github.com/yracine", style: "embedded", required: false, title: "More information...",
			description: "http://github.com/yracine/device-type.myecobee/blob/master/README.md"
		}
		section("When SmartThings' hello home mode change to [ex. 'Away', 'Home']") {
			input "newMode", "mode", metadata: [values: ["Away", "Home", "Night"]]
		}

		section("Change the following ecobee thermostat(s)...") {
			input "thermostats", "capability.thermostat", title: "Which thermostat(s)", multiple: true
		}
	}
	page(name: "selectProgram", title: "Ecobee Programs", content: "selectProgram")
	page(name: "Notifications", title: "Notifications Options", install: true, uninstall: true) {
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required:
				false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
        section([mobileOnly:true]) {
			label title: "Assign a name for this SmartApp", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
}


def selectProgram() {
    def ecobeePrograms = thermostats[0].currentClimateList.toString().minus('[').minus(']').tokenize(',')
	log.debug "programs: $ecobeePrograms"


	return dynamicPage(name: "selectProgram", title: "Select Ecobee Program", install: false, uninstall: true, nextPage:
		"Notifications") {
		section("Select Program") {
			input "givenClimate", "enum", title: "Change to this program?", options: ecobeePrograms, required: true
		}
	}
}


def installed() {
	subscribe(location, changeMode)
	subscribe(app, changeMode)
}

def updated() {
	unsubscribe()
	subscribe(location, changeMode)
	subscribe(app, changeMode)
}


def changeMode(evt) {
	def message

	log.info message
	message = "ecobeeChangeMode>Setting the thermostat(s) to $givenClimate.."
	send(message)
	if (newMode == "Away") {
		if (givenClimate.trim().toUpperCase() == 'AWAY') {
			thermostats?.away()

		} else thermostats?.setThisTstatClimate(givenClimate)

	} else {
		if (givenClimate.trim().toUpperCase() == 'HOME') {
			thermostats?.present()

		} else thermostats?.setThisTstatClimate(givenClimate)
	}
}

private send(msg) {
	if (sendPushMessage != "No") {
		log.debug("sending push message")
		sendPush(msg)
	}
	if (phone) {
		log.debug("sending text message")
		sendSms(phone, msg)
	}

	log.debug msg
}
