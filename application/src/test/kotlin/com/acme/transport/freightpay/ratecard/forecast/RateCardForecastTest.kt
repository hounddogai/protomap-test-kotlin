package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardForecastTest {
    @Test
    fun `starts in open`() {
        val item = newRateCardForecast("code-217", "name-506")
        assertEquals(RateCardForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRateCardForecast("code-217", "name-506")
        try {
            item.moveTo(RateCardForecastStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardForecastStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRateCardForecast("code-217", "name-506")
        item.status = RateCardForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(RateCardForecastStatus.OPEN) }
    }
}
