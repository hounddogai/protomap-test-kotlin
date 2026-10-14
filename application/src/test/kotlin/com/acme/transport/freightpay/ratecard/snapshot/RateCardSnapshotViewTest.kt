package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardSnapshot("code-891", "label-157")
        val view = toRateCardSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardSnapshotViews(mutableListOf<RateCardSnapshot>(item, item)).size, "views")
    }
}
