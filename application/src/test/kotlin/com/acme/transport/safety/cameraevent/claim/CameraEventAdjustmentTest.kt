package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventAdjustment("code-7", "title-551")
        assertEquals(CameraEventAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCameraEventAdjustment("code-7", "title-551")
        try {
            item.moveTo(CameraEventAdjustmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventAdjustmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventAdjustment("code-7", "title-551")
        item.status = CameraEventAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventAdjustmentStatus.NEW) }
    }
}
