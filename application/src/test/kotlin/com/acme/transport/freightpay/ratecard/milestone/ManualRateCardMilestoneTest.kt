package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRateCardMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newManualRateCardMilestone("code-245", "label-753")
        assertEquals(ManualRateCardMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newManualRateCardMilestone("code-245", "label-753")
        try {
            item.moveTo(ManualRateCardMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRateCardMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualRateCardMilestone("code-245", "label-753")
        item.status = ManualRateCardMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(ManualRateCardMilestoneStatus.NEW) }
    }
}
