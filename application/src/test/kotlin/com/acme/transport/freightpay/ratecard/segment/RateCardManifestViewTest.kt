package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardManifest("code-593", "name-796")
        val view = toRateCardManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardManifestViews(mutableListOf<RateCardManifest>(item, item)).size, "views")
    }
}
