package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIdlingForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIdlingForecast("code-29", "title-977")
        val view = toLegacyIdlingForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIdlingForecastViews(mutableListOf<LegacyIdlingForecast>(item, item)).size, "views")
    }
}
