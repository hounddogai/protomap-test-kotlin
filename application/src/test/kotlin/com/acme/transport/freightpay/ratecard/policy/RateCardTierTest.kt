package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardTierTest {
    @Test
    fun `starts in planned`() {
        val item = newRateCardTier("code-757", "title-926")
        assertEquals(RateCardTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRateCardTier("code-757", "title-926")
        try {
            item.moveTo(RateCardTierStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardTierStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRateCardTier("code-757", "title-926")
        item.status = RateCardTierStatus.CANCELLED
        assertFails("move") { item.moveTo(RateCardTierStatus.PLANNED) }
    }
}
