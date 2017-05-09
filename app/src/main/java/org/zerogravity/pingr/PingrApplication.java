/*
 *	Copyright 2014 Deepankar Bhardwaj 
 * 
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License. 
 *	You may obtain a copy of the License at 
 * 	
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 * 
 */

package org.zerogravity.pingr;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * @author bharddee Singleton application class
 */

@ReportsCrashes(
        formUri = "https://pingr.cloudant.com/acra-pingr-gh/_design/acra-storage/_update/report",
        // socketTimeout=2000,
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin = "bitiewhilymoreadortessli",
        formUriBasicAuthPassword = "7jmDbbywAXmEtSRUI3OKQIhI",
        // customReportContent = { ReportField.APP_VERSION_CODE},
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined,
        // adds a user text field input with this text resource as a label
        resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message
        // when the user accepts to send a report.
)
public class PingrApplication extends Application {

    public static int greenThreshold;
    public static int orangeThreshold;
    public static int redThreshold;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
