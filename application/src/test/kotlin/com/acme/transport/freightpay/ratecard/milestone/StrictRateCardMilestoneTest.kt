package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRateCardMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictRateCardMilestone("code-619", "label-413")
        assertEquals(StrictRateCardMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictRateCardMilestone("code-619", "label-413")
        try {
            item.moveTo(StrictRateCardMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRateCardMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictRateCardMilestone("code-619", "label-413")
        item.status = StrictRateCardMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictRateCardMilestoneStatus.REQUESTED) }
    }
}
