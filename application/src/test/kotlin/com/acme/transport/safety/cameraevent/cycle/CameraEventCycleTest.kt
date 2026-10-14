package com.acme.transport.safety.cameraevent.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newCameraEventCycle("code-691", "label-541")
        assertEquals(CameraEventCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCameraEventCycle("code-691", "label-541")
        try {
            item.moveTo(CameraEventCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCameraEventCycle("code-691", "label-541")
        item.status = CameraEventCycleStatus.CLOSED
        assertFails("move") { item.moveTo(CameraEventCycleStatus.DRAFT) }
    }
}
