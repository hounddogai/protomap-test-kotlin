package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRateCardRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedRateCardRollup("code-913", "name-953")
        assertEquals(BatchedRateCardRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBatchedRateCardRollup("code-913", "name-953")
        try {
            item.moveTo(BatchedRateCardRollupStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRateCardRollupStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedRateCardRollup("code-913", "name-953")
        item.status = BatchedRateCardRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedRateCardRollupStatus.PROPOSED) }
    }
}
