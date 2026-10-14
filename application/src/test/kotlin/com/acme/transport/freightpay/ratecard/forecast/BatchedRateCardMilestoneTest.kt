package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRateCardMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedRateCardMilestone("code-896", "title-956")
        assertEquals(BatchedRateCardMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBatchedRateCardMilestone("code-896", "title-956")
        try {
            item.moveTo(BatchedRateCardMilestoneStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRateCardMilestoneStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedRateCardMilestone("code-896", "title-956")
        item.status = BatchedRateCardMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedRateCardMilestoneStatus.IDLE) }
    }
}
