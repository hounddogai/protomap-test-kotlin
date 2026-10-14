package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newRateCardTarget("code-139", "name-527")
        assertEquals(RateCardTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRateCardTarget("code-139", "name-527")
        try {
            item.moveTo(RateCardTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRateCardTarget("code-139", "name-527")
        item.status = RateCardTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(RateCardTargetStatus.PLANNED) }
    }
}
