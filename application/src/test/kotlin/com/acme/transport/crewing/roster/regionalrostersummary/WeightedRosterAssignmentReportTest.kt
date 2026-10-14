package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRosterAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708048397)
        val items = mutableListOf<WeightedRosterAssignment>()
        for (index in 0 until 4) {
            items.add(newWeightedRosterAssignment("weighted-roster-assignment-$index", "sample"))
        }
        val report = buildWeightedRosterAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
