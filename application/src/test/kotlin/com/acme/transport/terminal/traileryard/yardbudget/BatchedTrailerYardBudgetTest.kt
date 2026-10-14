package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerYardBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedTrailerYardBudget("code-198", "name-997")
        assertEquals(BatchedTrailerYardBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedTrailerYardBudget("code-198", "name-997")
        try {
            item.moveTo(BatchedTrailerYardBudgetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerYardBudgetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedTrailerYardBudget("code-198", "name-997")
        item.status = BatchedTrailerYardBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedTrailerYardBudgetStatus.PROPOSED) }
    }
}
