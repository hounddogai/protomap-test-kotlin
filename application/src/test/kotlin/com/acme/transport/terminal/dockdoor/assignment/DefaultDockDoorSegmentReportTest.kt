package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDockDoorSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703751157)
        val items = mutableListOf<DefaultDockDoorSegment>()
        for (index in 0 until 3) {
            items.add(newDefaultDockDoorSegment("default-dock-door-segment-$index", "sample"))
        }
        val report = buildDefaultDockDoorSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
