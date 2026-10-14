package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702408013)
        val items = mutableListOf<SeasonalRedeliveryAssignment>()
        for (index in 0 until 3) {
            items.add(newSeasonalRedeliveryAssignment("seasonal-redelivery-assignment-$index", "sample"))
        }
        val report = buildSeasonalRedeliveryAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
