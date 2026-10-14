package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulForecast("code-398", "title-40")
        val view = toBackhaulForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulForecastViews(mutableListOf<BackhaulForecast>(item, item)).size, "views")
    }
}
