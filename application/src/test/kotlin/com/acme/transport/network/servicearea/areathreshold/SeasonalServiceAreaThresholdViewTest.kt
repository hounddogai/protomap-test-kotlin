package com.acme.transport.network.servicearea.areathreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalServiceAreaThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalServiceAreaThreshold("code-625", "name-928")
        val view = toSeasonalServiceAreaThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalServiceAreaThresholdViews(mutableListOf<SeasonalServiceAreaThreshold>(item, item)).size, "views")
    }
}
