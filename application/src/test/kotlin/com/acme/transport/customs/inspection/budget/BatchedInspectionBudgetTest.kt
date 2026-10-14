package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedInspectionBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedInspectionBudget("code-48", "title-471")
        assertEquals(BatchedInspectionBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedInspectionBudget("code-48", "title-471")
        try {
            item.moveTo(BatchedInspectionBudgetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedInspectionBudgetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedInspectionBudget("code-48", "title-471")
        item.status = BatchedInspectionBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedInspectionBudgetStatus.PROPOSED) }
    }
}
