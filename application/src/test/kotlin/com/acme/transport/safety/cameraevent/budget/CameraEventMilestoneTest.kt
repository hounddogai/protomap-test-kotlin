package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventMilestone("code-986", "title-817")
        assertEquals(CameraEventMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCameraEventMilestone("code-986", "title-817")
        try {
            item.moveTo(CameraEventMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventMilestone("code-986", "title-817")
        item.status = CameraEventMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventMilestoneStatus.IDLE) }
    }
}
