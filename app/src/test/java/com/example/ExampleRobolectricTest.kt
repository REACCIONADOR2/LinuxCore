package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DistroCatalog
import com.example.data.model.DistroTemplate
import com.example.data.model.VmInstance
import com.example.data.model.VmStatus
import com.example.engine.VirtualFileSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VirtuLinux", appName)
    }

    @Test
    fun `verify distro catalog contains major distributions`() {
        val templates = DistroCatalog.templates
        assertTrue(templates.isNotEmpty())
        assertNotNull(DistroCatalog.getById("ubuntu-24-lts"))
        assertNotNull(DistroCatalog.getById("arch-rolling"))
        assertNotNull(DistroCatalog.getById("alpine-musl"))
        assertNotNull(DistroCatalog.getById("debian-12"))
        assertNotNull(DistroCatalog.getById("kali-rolling"))
    }

    @Test
    fun `verify virtual file system initialization and operations`() {
        val vfs = VirtualFileSystem()
        val rootFiles = vfs.listFiles("/")
        assertTrue(rootFiles.any { it.name == "etc" })
        assertTrue(rootFiles.any { it.name == "home" })
        assertTrue(rootFiles.any { it.name == "mnt" })

        // Test creating file
        val created = vfs.createFile("/home/user", "test_file.txt", "Linux Kernel Emulation")
        assertTrue(created)
        val file = vfs.getFile("/home/user/test_file.txt")
        assertNotNull(file)
        assertEquals("Linux Kernel Emulation", file?.content)

        // Test updating file
        vfs.updateFileContent("/home/user/test_file.txt", "Updated Content")
        assertEquals("Updated Content", vfs.getFile("/home/user/test_file.txt")?.content)

        // Test deleting file
        val deleted = vfs.deleteFile("/home/user/test_file.txt")
        assertTrue(deleted)
        assertEquals(null, vfs.getFile("/home/user/test_file.txt"))
    }
}
