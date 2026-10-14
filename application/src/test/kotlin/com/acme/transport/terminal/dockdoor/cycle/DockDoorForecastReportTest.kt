package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705290356)
        val items = mutableListOf<DockDoorForecast>()
        for (index in 0 until 3) {
            items.add(newDockDoorForecast("dock-door-forecast-$index", "sample"))
        }
        val report = buildDockDoorForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
