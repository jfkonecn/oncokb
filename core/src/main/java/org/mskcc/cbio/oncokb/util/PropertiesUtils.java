/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.apiModels.CurationPlatformConfigs;
import org.mskcc.cbio.oncokb.apiModels.FirebaseConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author jgao
 */
public final class PropertiesUtils {
    private static Properties PROPERTIES;
    private PropertiesUtils() {
        throw new AssertionError();
    }

    public static String getProperties(String name) {
        if (name == null) {
            return null;
        }

        if (PROPERTIES == null) {
            try {
                String propFileName = "properties/config.properties";
                PROPERTIES = new Properties();
                InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(propFileName);

                if (inputStream != null) {
                    PROPERTIES.load(inputStream);
                } else {
                    System.out.println("property file '" + propFileName + "' not found in the classpath");
                }
                inputStream.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        // use system property if available, otherwise get from file
        return System.getProperty(name, PROPERTIES.get(name) == null ? null : (String)PROPERTIES.get(name));
    }

    public static boolean showSiteMaps() {
        return new Boolean(getProperties("show_sitemaps"));
    }

    public static String getCurationPlatformConfigs() {
        CurationPlatformConfigs curationPlatformConfigs = new CurationPlatformConfigs();
        curationPlatformConfigs.setApiLink(getProperties("curation_platform.api_link"));
        curationPlatformConfigs.setCurationLink(getProperties("curation_platform.curation_link"));
        curationPlatformConfigs.setPrivateApiLink(getProperties("curation_platform.private_link"));
        curationPlatformConfigs.setInternalPrivateApiLink(getProperties("curation_platform.internal_private_link"));
        curationPlatformConfigs.setPublicApiLink(getProperties("curation_platform.public_api_link"));
        curationPlatformConfigs.setInternalPublicApiLink(getProperties("curation_platform.internal_public_api_link"));
        curationPlatformConfigs.setWebsocketApiLink(getProperties("curation_platform.websocket_api_link"));
        curationPlatformConfigs.setProduction(Boolean.valueOf(getProperties("curation_platform.production")));
        curationPlatformConfigs.setTesting(Boolean.valueOf(getProperties("curation_platform.testing")));

        FirebaseConfig firebaseConfig = new FirebaseConfig();
        firebaseConfig.setApiKey(getProperties("curation_platform.firebase_config.api_key"));
        firebaseConfig.setAuthDomain(getProperties("curation_platform.firebase_config.auth_domain"));
        firebaseConfig.setDatabaseURL(getProperties("curation_platform.firebase_config.database_url"));
        firebaseConfig.setProjectId(getProperties("curation_platform.firebase_config.project_id"));
        firebaseConfig.setStorageBucket(getProperties("curation_platform.firebase_config.storage_bucket"));
        firebaseConfig.setMessagingSenderId(getProperties("curation_platform.firebase_config.messaging_sender_id"));
        curationPlatformConfigs.setFirebaseConfig(firebaseConfig);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(curationPlatformConfigs);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
