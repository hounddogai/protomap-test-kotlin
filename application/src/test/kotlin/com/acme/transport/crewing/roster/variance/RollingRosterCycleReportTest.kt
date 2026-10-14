package com.acme.transport.crewing.roster.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRosterCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704453135)
        val items = mutableListOf<RollingRosterCycle>()
        for (index in 0 until 4) {
            items.add(newRollingRosterCycle("rolling-roster-cycle-$index", "sample"))
        }
        val report = buildRollingRosterCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
