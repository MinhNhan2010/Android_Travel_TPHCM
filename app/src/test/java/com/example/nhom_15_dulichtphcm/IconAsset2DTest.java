package com.example.nhom_15_dulichtphcm;

import com.example.nhom_15_dulichtphcm.ui.components.AppIcons2D;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IconAsset2DTest {

    private File locateDrawableDir() {
        File cwd = new File(System.getProperty("user.dir", "."));
        File direct = new File(cwd, "src/main/res/drawable");
        if (direct.isDirectory()) return direct;
        File fromRoot = new File(cwd, "app/src/main/res/drawable");
        if (fromRoot.isDirectory()) return fromRoot;
        return direct;
    }

    @Test
    public void all16Custom2dVectorDrawables_existAndContainVectorXml() throws Exception {
        assertEquals(16, AppIcons2D.ALL_REQUIRED_2D_DRAWABLES.size());
        File drawableDir = locateDrawableDir();
        assertTrue("Drawable directory must exist: " + drawableDir.getAbsolutePath(), drawableDir.isDirectory());

        for (String fileName : AppIcons2D.ALL_REQUIRED_2D_DRAWABLES) {
            File f = new File(drawableDir, fileName);
            assertTrue("Missing 2D vector drawable: " + fileName, f.isFile());
            String xml = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            assertTrue(fileName + " must declare <vector", xml.contains("<vector"));
            assertTrue(fileName + " must declare <path", xml.contains("<path"));
        }
    }
}
