package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServiceAreaBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedServiceAreaBudget("code-973", "title-357")
        assertEquals(BatchedServiceAreaBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedServiceAreaBudget("code-973", "title-357")
        try {
            item.moveTo(BatchedServiceAreaBudgetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServiceAreaBudgetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedServiceAreaBudget("code-973", "title-357")
        item.status = BatchedServiceAreaBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedServiceAreaBudgetStatus.DRAFT) }
    }
}
