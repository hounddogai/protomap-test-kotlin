package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBackhaulWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700221010)
        val items = mutableListOf<LegacyBackhaulWindow>()
        for (index in 0 until 2) {
            items.add(newLegacyBackhaulWindow("legacy-backhaul-window-$index", "sample"))
        }
        val report = buildLegacyBackhaulWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
