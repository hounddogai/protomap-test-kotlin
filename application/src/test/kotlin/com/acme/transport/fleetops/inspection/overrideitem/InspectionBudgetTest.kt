package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newInspectionBudget("code-643", "title-36")
        assertEquals(InspectionBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newInspectionBudget("code-643", "title-36")
        try {
            item.moveTo(InspectionBudgetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionBudgetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newInspectionBudget("code-643", "title-36")
        item.status = InspectionBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(InspectionBudgetStatus.IDLE) }
    }
}
