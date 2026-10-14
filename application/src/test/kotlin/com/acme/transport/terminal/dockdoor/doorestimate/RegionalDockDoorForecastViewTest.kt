package com.acme.transport.terminal.dockdoor.doorestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDockDoorForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDockDoorForecast("code-670", "label-820")
        val view = toRegionalDockDoorForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDockDoorForecastViews(mutableListOf<RegionalDockDoorForecast>(item, item)).size, "views")
    }
}
