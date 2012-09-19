/*******************************************************************************
* Emergency.java.java, Created: Apr 24, 2012
*
* Part of Muni Project
*
* Copyright (c) 2012 : NDS Limited
*
* P R O P R I E T A R Y &amp; C O N F I D E N T I A L
*
* The copyright of this code and related documentation together with any
* other associated intellectual property rights are vested in NDS Limited
* and may not be used except in accordance with the terms of the licence
* that you have entered into with NDS Limited. Use of this material without
* an express licence from NDS Limited shall be an infringement of copyright
* and any other intellectual property rights that may be incorporated with
* this material.
*
* ******************************************************************************
* ******     Please Check GIT for revision/modification history    *******
* ******************************************************************************
*/
package com.safecell.model;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author uttama
 *
 */
public class Emergency {

    public Emergency() {
    }

    public static final class Emergencies implements BaseColumns {
        private Emergencies() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + EmergencyProvider.AUTHORITY + "/emergency");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jwei512.emergencies";

        public static final String EMERGENCY_ID = "_id";

        public static final String NAME = "name";

        public static final String NUMBER = "number";
    }

}