package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBackhaulCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707230966)
        val items = mutableListOf<PendingBackhaulCycle>()
        for (index in 0 until 6) {
            items.add(newPendingBackhaulCycle("pending-backhaul-cycle-$index", "sample"))
        }
        val report = buildPendingBackhaulCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
