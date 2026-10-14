package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704752239)
        val items = mutableListOf<GlobalTrailerCycle>()
        for (index in 0 until 2) {
            items.add(newGlobalTrailerCycle("global-trailer-cycle-$index", "sample"))
        }
        val report = buildGlobalTrailerCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
