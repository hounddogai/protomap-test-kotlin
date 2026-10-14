package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRateCardDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRateCardDigest("code-505", "title-970")
        val view = toWeightedRateCardDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRateCardDigestViews(mutableListOf<WeightedRateCardDigest>(item, item)).size, "views")
    }
}
