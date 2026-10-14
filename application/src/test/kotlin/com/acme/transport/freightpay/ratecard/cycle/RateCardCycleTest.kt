package com.acme.transport.freightpay.ratecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardCycleTest {
    @Test
    fun `starts in open`() {
        val item = newRateCardCycle("code-791", "label-684")
        assertEquals(RateCardCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRateCardCycle("code-791", "label-684")
        try {
            item.moveTo(RateCardCycleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardCycleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRateCardCycle("code-791", "label-684")
        item.status = RateCardCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(RateCardCycleStatus.OPEN) }
    }
}
