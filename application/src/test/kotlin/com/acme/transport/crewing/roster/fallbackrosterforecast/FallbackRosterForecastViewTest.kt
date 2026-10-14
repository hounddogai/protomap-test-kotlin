package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRosterForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRosterForecast("code-323", "title-810")
        val view = toFallbackRosterForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRosterForecastViews(mutableListOf<FallbackRosterForecast>(item, item)).size, "views")
    }
}
