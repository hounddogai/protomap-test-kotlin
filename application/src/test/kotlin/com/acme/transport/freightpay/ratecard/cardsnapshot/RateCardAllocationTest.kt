package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newRateCardAllocation("code-602", "title-422")
        assertEquals(RateCardAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRateCardAllocation("code-602", "title-422")
        try {
            item.moveTo(RateCardAllocationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardAllocationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRateCardAllocation("code-602", "title-422")
        item.status = RateCardAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(RateCardAllocationStatus.PLANNED) }
    }
}
