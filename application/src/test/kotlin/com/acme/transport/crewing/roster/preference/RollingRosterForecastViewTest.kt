package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRosterForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRosterForecast("code-468", "title-996")
        val view = toRollingRosterForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRosterForecastViews(mutableListOf<RollingRosterForecast>(item, item)).size, "views")
    }
}
