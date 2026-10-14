package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDropForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDropForecast("code-61", "name-647")
        val view = toScheduledDropForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDropForecastViews(mutableListOf<ScheduledDropForecast>(item, item)).size, "views")
    }
}
