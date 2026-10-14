package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedEvChargeScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedEvChargeScore("code-567", "name-141")
        assertEquals(BatchedEvChargeScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedEvChargeScore("code-567", "name-141")
        try {
            item.moveTo(BatchedEvChargeScoreStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedEvChargeScoreStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedEvChargeScore("code-567", "name-141")
        item.status = BatchedEvChargeScoreStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedEvChargeScoreStatus.DRAFT) }
    }
}
