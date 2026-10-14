package com.acme.transport.freightpay.ratecard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newRateCardAdjustment("code-884", "name-422")
        assertEquals(RateCardAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRateCardAdjustment("code-884", "name-422")
        try {
            item.moveTo(RateCardAdjustmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardAdjustmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRateCardAdjustment("code-884", "name-422")
        item.status = RateCardAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(RateCardAdjustmentStatus.PLANNED) }
    }
}
