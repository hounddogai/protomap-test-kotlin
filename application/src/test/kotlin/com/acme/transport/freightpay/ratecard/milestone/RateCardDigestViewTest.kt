package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardDigest("code-623", "label-557")
        val view = toRateCardDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardDigestViews(mutableListOf<RateCardDigest>(item, item)).size, "views")
    }
}
