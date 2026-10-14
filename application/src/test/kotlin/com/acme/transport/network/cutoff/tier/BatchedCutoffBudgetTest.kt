package com.acme.transport.network.cutoff.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCutoffBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedCutoffBudget("code-204", "title-868")
        assertEquals(BatchedCutoffBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedCutoffBudget("code-204", "title-868")
        try {
            item.moveTo(BatchedCutoffBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCutoffBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedCutoffBudget("code-204", "title-868")
        item.status = BatchedCutoffBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedCutoffBudgetStatus.REQUESTED) }
    }
}
