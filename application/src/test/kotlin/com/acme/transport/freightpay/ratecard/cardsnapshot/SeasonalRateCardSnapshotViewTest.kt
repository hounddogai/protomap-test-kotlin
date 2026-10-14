package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRateCardSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRateCardSnapshot("code-73", "name-206")
        val view = toSeasonalRateCardSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRateCardSnapshotViews(mutableListOf<SeasonalRateCardSnapshot>(item, item)).size, "views")
    }
}
