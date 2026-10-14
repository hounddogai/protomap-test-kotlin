package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706698044)
        val items = mutableListOf<DockDoorPolicy>()
        for (index in 0 until 3) {
            items.add(newDockDoorPolicy("dock-door-policy-$index", "sample"))
        }
        val report = buildDockDoorPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
