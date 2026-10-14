package com.acme.transport.freightpay.ratecard.cardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRateCardForecastTest {
    @Test
    fun `starts in created`() {
        val item = newLenientRateCardForecast("code-510", "title-341")
        assertEquals(LenientRateCardForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientRateCardForecast("code-510", "title-341")
        try {
            item.moveTo(LenientRateCardForecastStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRateCardForecastStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientRateCardForecast("code-510", "title-341")
        item.status = LenientRateCardForecastStatus.RETIRED
        assertFails("move") { item.moveTo(LenientRateCardForecastStatus.CREATED) }
    }
}
