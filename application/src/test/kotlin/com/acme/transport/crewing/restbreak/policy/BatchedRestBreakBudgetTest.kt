package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRestBreakBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedRestBreakBudget("code-555", "name-467")
        assertEquals(BatchedRestBreakBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBatchedRestBreakBudget("code-555", "name-467")
        try {
            item.moveTo(BatchedRestBreakBudgetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRestBreakBudgetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedRestBreakBudget("code-555", "name-467")
        item.status = BatchedRestBreakBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedRestBreakBudgetStatus.PROPOSED) }
    }
}
