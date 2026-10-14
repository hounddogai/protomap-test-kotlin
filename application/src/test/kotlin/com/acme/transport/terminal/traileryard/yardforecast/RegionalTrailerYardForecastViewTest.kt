package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalTrailerYardForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalTrailerYardForecast("code-401", "title-475")
        val view = toRegionalTrailerYardForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalTrailerYardForecastViews(mutableListOf<RegionalTrailerYardForecast>(item, item)).size, "views")
    }
}
