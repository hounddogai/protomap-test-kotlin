package com.acme.transport.crewing.roster.threshold

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709806514)
        val items = mutableListOf<RosterThreshold>()
        for (index in 0 until 5) {
            items.add(newRosterThreshold("roster-threshold-$index", "sample"))
        }
        val report = buildRosterThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
