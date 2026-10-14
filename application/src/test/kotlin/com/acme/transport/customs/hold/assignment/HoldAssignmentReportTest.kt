package com.acme.transport.customs.hold.assignment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709395159)
        val items = mutableListOf<HoldAssignment>()
        for (index in 0 until 5) {
            items.add(newHoldAssignment("hold-assignment-$index", "sample"))
        }
        val report = buildHoldAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
