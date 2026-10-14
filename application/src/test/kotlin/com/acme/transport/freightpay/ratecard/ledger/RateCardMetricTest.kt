package com.acme.transport.freightpay.ratecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newRateCardMetric("code-864", "title-352")
        assertEquals(RateCardMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRateCardMetric("code-864", "title-352")
        try {
            item.moveTo(RateCardMetricStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardMetricStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRateCardMetric("code-864", "title-352")
        item.status = RateCardMetricStatus.CLOSED
        assertFails("move") { item.moveTo(RateCardMetricStatus.DRAFT) }
    }
}
