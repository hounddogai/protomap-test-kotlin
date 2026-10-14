package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTenderEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedTenderEstimate("code-112", "title-450")
        assertEquals(BatchedTenderEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedTenderEstimate("code-112", "title-450")
        try {
            item.moveTo(BatchedTenderEstimateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTenderEstimateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedTenderEstimate("code-112", "title-450")
        item.status = BatchedTenderEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedTenderEstimateStatus.REQUESTED) }
    }
}
