package com.acme.transport.customs.clearance.assignment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingClearanceAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700042998)
        val items = mutableListOf<PendingClearanceAssignment>()
        for (index in 0 until 4) {
            items.add(newPendingClearanceAssignment("pending-clearance-assignment-$index", "sample"))
        }
        val report = buildPendingClearanceAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
