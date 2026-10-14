package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newCameraEventAllocation("code-484", "name-355")
        assertEquals(CameraEventAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCameraEventAllocation("code-484", "name-355")
        try {
            item.moveTo(CameraEventAllocationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventAllocationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCameraEventAllocation("code-484", "name-355")
        item.status = CameraEventAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(CameraEventAllocationStatus.PLANNED) }
    }
}
