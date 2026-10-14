package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelForecast("code-931", "name-745")
        val view = toParcelForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelForecastViews(mutableListOf<ParcelForecast>(item, item)).size, "views")
    }
}
