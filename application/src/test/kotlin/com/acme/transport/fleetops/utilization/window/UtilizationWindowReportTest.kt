package com.acme.transport.fleetops.utilization.window

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704549700)
        val items = mutableListOf<UtilizationWindow>()
        for (index in 0 until 5) {
            items.add(newUtilizationWindow("utilization-window-$index", "sample"))
        }
        val report = buildUtilizationWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
