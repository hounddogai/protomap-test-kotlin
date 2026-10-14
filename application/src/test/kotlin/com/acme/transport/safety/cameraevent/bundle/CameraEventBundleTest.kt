package com.acme.transport.safety.cameraevent.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventBundleTest {
    @Test
    fun `starts in open`() {
        val item = newCameraEventBundle("code-641", "title-97")
        assertEquals(CameraEventBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCameraEventBundle("code-641", "title-97")
        try {
            item.moveTo(CameraEventBundleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventBundleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCameraEventBundle("code-641", "title-97")
        item.status = CameraEventBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(CameraEventBundleStatus.OPEN) }
    }
}
