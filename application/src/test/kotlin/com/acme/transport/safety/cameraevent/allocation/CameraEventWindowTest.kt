package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventWindowTest {
    @Test
    fun `starts in open`() {
        val item = newCameraEventWindow("code-647", "label-937")
        assertEquals(CameraEventWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCameraEventWindow("code-647", "label-937")
        try {
            item.moveTo(CameraEventWindowStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventWindowStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCameraEventWindow("code-647", "label-937")
        item.status = CameraEventWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(CameraEventWindowStatus.OPEN) }
    }
}
