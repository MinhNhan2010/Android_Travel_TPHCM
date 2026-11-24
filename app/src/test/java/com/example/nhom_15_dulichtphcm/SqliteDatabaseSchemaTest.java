package com.example.nhom_15_dulichtphcm;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelContract;
import com.example.nhom_15_dulichtphcm.data.local.TicketWalletManager;
import com.example.nhom_15_dulichtphcm.data.repository.MetroTransitData;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SqliteDatabaseSchemaTest {

    private File locateAppDir() {
        File cwd = new File(System.getProperty("user.dir", "."));
        if (new File(cwd, "src/main/java").isDirectory()) {
            return cwd;
        }
        return new File(cwd, "app");
    }

    @Test
    public void sqliteSchemaContract_definesAll18RelationalTables() {
        assertEquals("saigon_travel.db", SaigonTravelContract.DATABASE_NAME);
        assertEquals(18, SaigonTravelContract.ALL_TABLES.size());
        assertTrue(SaigonTravelContract.ALL_CREATE_STATEMENTS.size() >= 18);
        assertTrue(SaigonTravelContract.ALL_TABLES.contains(SaigonTravelContract.TABLE_TICKETS));
        assertTrue(SaigonTravelContract.ALL_TABLES.contains(SaigonTravelContract.TABLE_TRAVEL_JOURNALS));
        assertTrue(SaigonTravelContract.ALL_TABLES.contains(SaigonTravelContract.TABLE_PACKING_ITEMS));
        for (String table : SaigonTravelContract.ALL_TABLES) {
            boolean foundCreate = false;
            for (String ddl : SaigonTravelContract.ALL_CREATE_STATEMENTS) {
                if (ddl.contains("CREATE TABLE IF NOT EXISTS " + table)) {
                    foundCreate = true;
                    break;
                }
            }
            assertTrue("Missing CREATE TABLE DDL for: " + table, foundCreate);
        }
    }

    @Test
    public void codebaseArchitecture_is100PercentJavaXmlAndSqliteWithoutSharedPreferences() throws Exception {
        File appDir = locateAppDir();
        File srcRoot = new File(appDir, "src");
        File mainJavaDir = new File(appDir, "src/main/java");
        File layoutDir = new File(appDir, "src/main/res/layout");

        List<File> kotlinFiles = new ArrayList<>();
        collectFilesByExtension(srcRoot, ".kt", kotlinFiles);
        assertEquals("Zero Kotlin files should remain under app/src", 0, kotlinFiles.size());

        List<File> xmlLayouts = new ArrayList<>();
        collectFilesByExtension(layoutDir, ".xml", xmlLayouts);
        assertTrue("Expected >= 38 XML layout files in res/layout", xmlLayouts.size() >= 38);

        List<File> javaFiles = new ArrayList<>();
        collectFilesByExtension(mainJavaDir, ".java", javaFiles);
        assertFalse(javaFiles.isEmpty());
        for (File jf : javaFiles) {
            String src = new String(Files.readAllBytes(jf.toPath()), StandardCharsets.UTF_8);
            assertFalse("No SharedPreferences import allowed in " + jf.getName(),
                    src.contains("android.content.SharedPreferences") || src.contains("getSharedPreferences("));
        }
    }

    @Test
    public void metroTransitAndTicketCalculations_areAccurate() {
        assertEquals(14, MetroTransitData.METRO_LINE_1.size());
        assertEquals(5, MetroTransitData.SAIGON_WATERBUS.size());
        assertEquals(7_000, MetroTransitData.calculateMetroCashFareVnd(0, 2));
        assertEquals(6_000, MetroTransitData.calculateMetroCashlessFareVnd(0, 2));
        assertEquals(20_000, MetroTransitData.calculateMetroCashFareVnd(0, 13));
        assertEquals(19_000, MetroTransitData.calculateMetroCashlessFareVnd(0, 13));

        assertEquals(40_000L, TicketWalletManager.parseAdultPriceVnd("40.000 VNĐ / người"));
        assertEquals(0L, TicketWalletManager.parseAdultPriceVnd("Miễn phí (Free)"));
        assertEquals(110_000L, TicketWalletManager.calculateTotalVnd(40_000L, 2, 1, 1));
    }

    private void collectFilesByExtension(File dir, String ext, List<File> out) {
        if (dir == null || !dir.exists()) return;
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File c : children) {
            if (c.isDirectory()) {
                collectFilesByExtension(c, ext, out);
            } else if (c.getName().endsWith(ext)) {
                out.add(c);
            }
        }
    }
}
