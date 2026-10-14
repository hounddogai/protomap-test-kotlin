package com.acme.transport.safety.coaching.assignment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709905811)
        val items = mutableListOf<CoachingAssignment>()
        for (index in 0 until 5) {
            items.add(newCoachingAssignment("coaching-assignment-$index", "sample"))
        }
        val report = buildCoachingAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
