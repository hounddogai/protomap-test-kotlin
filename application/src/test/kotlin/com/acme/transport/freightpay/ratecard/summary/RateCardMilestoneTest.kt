package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newRateCardMilestone("code-844", "title-929")
        assertEquals(RateCardMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRateCardMilestone("code-844", "title-929")
        try {
            item.moveTo(RateCardMilestoneStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardMilestoneStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRateCardMilestone("code-844", "title-929")
        item.status = RateCardMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(RateCardMilestoneStatus.DRAFT) }
    }
}
