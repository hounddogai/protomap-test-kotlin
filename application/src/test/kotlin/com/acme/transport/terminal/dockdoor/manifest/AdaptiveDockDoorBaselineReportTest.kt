package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDockDoorBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707999094)
        val items = mutableListOf<AdaptiveDockDoorBaseline>()
        for (index in 0 until 4) {
            items.add(newAdaptiveDockDoorBaseline("adaptive-dock-door-baseline-$index", "sample"))
        }
        val report = buildAdaptiveDockDoorBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
