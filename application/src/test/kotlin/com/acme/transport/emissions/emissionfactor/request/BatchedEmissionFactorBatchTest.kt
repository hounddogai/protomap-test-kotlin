package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedEmissionFactorBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedEmissionFactorBatch("code-191", "name-991")
        assertEquals(BatchedEmissionFactorBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedEmissionFactorBatch("code-191", "name-991")
        try {
            item.moveTo(BatchedEmissionFactorBatchStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedEmissionFactorBatchStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedEmissionFactorBatch("code-191", "name-991")
        item.status = BatchedEmissionFactorBatchStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedEmissionFactorBatchStatus.DRAFT) }
    }
}
