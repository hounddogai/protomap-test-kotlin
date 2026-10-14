package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliveryThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliveryThreshold("code-928", "name-907")
        val view = toSeasonalRedeliveryThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliveryThresholdViews(mutableListOf<SeasonalRedeliveryThreshold>(item, item)).size, "views")
    }
}
