package com.acme.transport.safety.cameraevent.eventbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventThreshold("code-450", "label-443")
        assertEquals(CameraEventThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCameraEventThreshold("code-450", "label-443")
        try {
            item.moveTo(CameraEventThresholdStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventThresholdStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventThreshold("code-450", "label-443")
        item.status = CameraEventThresholdStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventThresholdStatus.NEW) }
    }
}
