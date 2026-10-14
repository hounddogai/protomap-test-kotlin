package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredZoneForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredZoneForecast("code-966", "label-590")
        val view = toTieredZoneForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredZoneForecastViews(mutableListOf<TieredZoneForecast>(item, item)).size, "views")
    }
}
