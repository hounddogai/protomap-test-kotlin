package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedEmissionFactorRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedEmissionFactorRequest("code-917", "title-874")
        assertEquals(BatchedEmissionFactorRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedEmissionFactorRequest("code-917", "title-874")
        try {
            item.moveTo(BatchedEmissionFactorRequestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedEmissionFactorRequestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedEmissionFactorRequest("code-917", "title-874")
        item.status = BatchedEmissionFactorRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedEmissionFactorRequestStatus.PROPOSED) }
    }
}
