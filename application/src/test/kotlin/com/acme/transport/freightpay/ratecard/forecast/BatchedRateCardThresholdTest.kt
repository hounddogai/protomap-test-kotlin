package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRateCardThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedRateCardThreshold("code-704", "name-965")
        assertEquals(BatchedRateCardThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newBatchedRateCardThreshold("code-704", "name-965")
        try {
            item.moveTo(BatchedRateCardThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRateCardThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedRateCardThreshold("code-704", "name-965")
        item.status = BatchedRateCardThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedRateCardThresholdStatus.REQUESTED) }
    }
}
