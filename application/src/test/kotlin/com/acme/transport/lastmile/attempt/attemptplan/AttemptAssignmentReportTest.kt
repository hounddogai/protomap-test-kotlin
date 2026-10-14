package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708289674)
        val items = mutableListOf<AttemptAssignment>()
        for (index in 0 until 4) {
            items.add(newAttemptAssignment("attempt-assignment-$index", "sample"))
        }
        val report = buildAttemptAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
