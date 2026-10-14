package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventPlanTest {
    @Test
    fun `starts in open`() {
        val item = newCameraEventPlan("code-345", "name-357")
        assertEquals(CameraEventPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCameraEventPlan("code-345", "name-357")
        try {
            item.moveTo(CameraEventPlanStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventPlanStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCameraEventPlan("code-345", "name-357")
        item.status = CameraEventPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(CameraEventPlanStatus.OPEN) }
    }
}
