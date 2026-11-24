package com.example.nhom_15_dulichtphcm;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelContract;
import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContextAndVerifySqliteDatabasePath() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.nhom_15_dulichtphcm", appContext.getPackageName());

        SaigonTravelDatabaseHelper helper = SaigonTravelDatabaseHelper.getInstance(appContext);
        assertNotNull(helper.getWritableDatabase());

        File dbFile = appContext.getDatabasePath(SaigonTravelContract.DATABASE_NAME);
        assertTrue(dbFile.getAbsolutePath().endsWith("databases" + File.separator + "saigon_travel.db"));
        assertTrue(dbFile.exists());
    }
}
