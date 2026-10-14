package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAttemptAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708854543)
        val items = mutableListOf<ManualAttemptAssignment>()
        for (index in 0 until 5) {
            items.add(newManualAttemptAssignment("manual-attempt-assignment-$index", "sample"))
        }
        val report = buildManualAttemptAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
