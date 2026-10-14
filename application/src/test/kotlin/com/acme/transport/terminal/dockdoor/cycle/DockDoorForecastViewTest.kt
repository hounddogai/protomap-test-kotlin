package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorForecast("code-771", "name-30")
        val view = toDockDoorForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorForecastViews(mutableListOf<DockDoorForecast>(item, item)).size, "views")
    }
}
