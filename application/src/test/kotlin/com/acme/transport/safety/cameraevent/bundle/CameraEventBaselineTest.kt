package com.acme.transport.safety.cameraevent.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventBaseline("code-569", "label-241")
        assertEquals(CameraEventBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newCameraEventBaseline("code-569", "label-241")
        try {
            item.moveTo(CameraEventBaselineStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventBaselineStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventBaseline("code-569", "label-241")
        item.status = CameraEventBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventBaselineStatus.IDLE) }
    }
}
