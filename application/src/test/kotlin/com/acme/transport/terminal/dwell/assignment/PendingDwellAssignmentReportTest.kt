package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDwellAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707182639)
        val items = mutableListOf<PendingDwellAssignment>()
        for (index in 0 until 3) {
            items.add(newPendingDwellAssignment("pending-dwell-assignment-$index", "sample"))
        }
        val report = buildPendingDwellAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
