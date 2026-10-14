package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRosterForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRosterForecast("code-226", "name-550")
        val view = toLenientRosterForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRosterForecastViews(mutableListOf<LenientRosterForecast>(item, item)).size, "views")
    }
}
