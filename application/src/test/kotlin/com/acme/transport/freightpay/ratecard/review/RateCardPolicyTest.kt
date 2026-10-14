package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newRateCardPolicy("code-654", "title-367")
        assertEquals(RateCardPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRateCardPolicy("code-654", "title-367")
        try {
            item.moveTo(RateCardPolicyStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardPolicyStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRateCardPolicy("code-654", "title-367")
        item.status = RateCardPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(RateCardPolicyStatus.DRAFT) }
    }
}
