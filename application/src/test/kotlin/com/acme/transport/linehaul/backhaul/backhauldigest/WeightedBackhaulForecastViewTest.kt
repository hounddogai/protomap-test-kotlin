package com.acme.transport.linehaul.backhaul.backhauldigest

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedBackhaulForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedBackhaulForecast("code-736", "title-277")
        val view = toWeightedBackhaulForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedBackhaulForecastViews(mutableListOf<WeightedBackhaulForecast>(item, item)).size, "views")
    }
}
