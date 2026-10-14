package com.acme.transport.lastmile.drop.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class DropForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropForecast("code-390", "title-962")
        val view = toDropForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropForecastViews(mutableListOf<DropForecast>(item, item)).size, "views")
    }
}
