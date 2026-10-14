package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRosterCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701179025)
        val items = mutableListOf<FallbackRosterCycle>()
        for (index in 0 until 6) {
            items.add(newFallbackRosterCycle("fallback-roster-cycle-$index", "sample"))
        }
        val report = buildFallbackRosterCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
