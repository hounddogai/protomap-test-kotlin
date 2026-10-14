package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventGroup("code-321", "name-806")
        assertEquals(CameraEventGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newCameraEventGroup("code-321", "name-806")
        try {
            item.moveTo(CameraEventGroupStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventGroupStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventGroup("code-321", "name-806")
        item.status = CameraEventGroupStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventGroupStatus.IDLE) }
    }
}
