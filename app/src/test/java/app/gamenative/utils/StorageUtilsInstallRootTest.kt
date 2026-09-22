package app.gamenative.utils

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageUtilsInstallRootTest {

    @Test
    fun `primary emulated volume has no public install root`() {
        val dir = File("/storage/emulated/0/Android/data/app.gamenative/files")
        assertNull(StorageUtils.publicInstallRoot(dir))
    }

    @Test
    fun `removable volume maps to public install root`() {
        val dir = File("/storage/1234-5678/Android/data/app.gamenative/files")
        assertEquals("/storage/1234-5678/GameNative", StorageUtils.publicInstallRoot(dir)?.absolutePath)
    }

    @Test
    fun `path without android data has no public install root`() {
        val dir = File("/storage/1234-5678/GameNative/Steam")
        assertNull(StorageUtils.publicInstallRoot(dir))
    }

    @Test
    fun `legacy dir on removable volume moves to public root`() {
        val tmp = File.createTempFile("storage", "").let {
            it.delete()
            it
        }
        val legacy = File(tmp, "storage/1234-5678/Android/data/app.gamenative/files/Steam/steamapps/common/MyGame")
        assertTrue(legacy.mkdirs())

        val resolved = StorageUtils.resolveLegacyGameDir(legacy.absolutePath)

        val expected = File(tmp, "storage/1234-5678/GameNative/Steam/steamapps/common/MyGame")
        assertEquals(expected.absolutePath, resolved)
        assertTrue(expected.isDirectory)
        tmp.deleteRecursively()
    }

    @Test
    fun `missing primary public game dir is left alone`() {
        val tmp = File.createTempFile("sandbox", "").let {
            it.delete()
            it
        }
        val sandbox = File(tmp, "Android/data/app.gamenative/files")
        val src = File("/storage/emulated/0/GameNative/Steam/steamapps/common/MyGame")

        assertEquals(src.absolutePath, StorageUtils.migratePublicPrimaryDir(src.absolutePath, sandbox))
        assertTrue(StorageUtils.isPrimaryPublicInstallRoot("/storage/emulated/0/GameNative"))
        tmp.deleteRecursively()
    }
}
