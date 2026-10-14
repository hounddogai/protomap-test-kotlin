package com.acme.transport.emissions.emissionfactor.factorproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEmissionFactorBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackEmissionFactorBatch("code-61", "name-785")
        assertEquals(FallbackEmissionFactorBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFallbackEmissionFactorBatch("code-61", "name-785")
        try {
            item.moveTo(FallbackEmissionFactorBatchStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEmissionFactorBatchStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackEmissionFactorBatch("code-61", "name-785")
        item.status = FallbackEmissionFactorBatchStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackEmissionFactorBatchStatus.DRAFT) }
    }
}
