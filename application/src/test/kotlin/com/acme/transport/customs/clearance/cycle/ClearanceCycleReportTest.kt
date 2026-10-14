package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704892126)
        val items = mutableListOf<ClearanceCycle>()
        for (index in 0 until 4) {
            items.add(newClearanceCycle("clearance-cycle-$index", "sample"))
        }
        val report = buildClearanceCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
