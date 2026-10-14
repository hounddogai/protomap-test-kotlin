package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventBudget("code-985", "label-566")
        assertEquals(CameraEventBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCameraEventBudget("code-985", "label-566")
        try {
            item.moveTo(CameraEventBudgetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventBudgetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventBudget("code-985", "label-566")
        item.status = CameraEventBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventBudgetStatus.IDLE) }
    }
}
