package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliveryEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliveryEstimate("code-686", "label-553")
        val view = toSeasonalRedeliveryEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliveryEstimateViews(mutableListOf<SeasonalRedeliveryEstimate>(item, item)).size, "views")
    }
}
