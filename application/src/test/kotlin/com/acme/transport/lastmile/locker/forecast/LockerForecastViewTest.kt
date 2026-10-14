package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerForecast("code-224", "label-537")
        val view = toLockerForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerForecastViews(mutableListOf<LockerForecast>(item, item)).size, "views")
    }
}
