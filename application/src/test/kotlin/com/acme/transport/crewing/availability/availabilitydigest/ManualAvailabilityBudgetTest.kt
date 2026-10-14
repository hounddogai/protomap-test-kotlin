package com.acme.transport.crewing.availability.availabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAvailabilityBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newManualAvailabilityBudget("code-746", "label-335")
        assertEquals(ManualAvailabilityBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newManualAvailabilityBudget("code-746", "label-335")
        try {
            item.moveTo(ManualAvailabilityBudgetStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAvailabilityBudgetStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualAvailabilityBudget("code-746", "label-335")
        item.status = ManualAvailabilityBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(ManualAvailabilityBudgetStatus.IDLE) }
    }
}
