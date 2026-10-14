package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterForecast("code-222", "label-569")
        val view = toRosterForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterForecastViews(mutableListOf<RosterForecast>(item, item)).size, "views")
    }
}
