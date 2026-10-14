package com.acme.transport.safety.cameraevent.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventNotice("code-545", "label-852")
        assertEquals(CameraEventNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCameraEventNotice("code-545", "label-852")
        try {
            item.moveTo(CameraEventNoticeStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventNoticeStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventNotice("code-545", "label-852")
        item.status = CameraEventNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventNoticeStatus.IDLE) }
    }
}
