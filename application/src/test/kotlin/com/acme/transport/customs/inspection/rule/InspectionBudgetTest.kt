package com.acme.transport.customs.inspection.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionBudget("code-853", "label-517")
        assertEquals(InspectionBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newInspectionBudget("code-853", "label-517")
        try {
            item.moveTo(InspectionBudgetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionBudgetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionBudget("code-853", "label-517")
        item.status = InspectionBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionBudgetStatus.REQUESTED) }
    }
}
