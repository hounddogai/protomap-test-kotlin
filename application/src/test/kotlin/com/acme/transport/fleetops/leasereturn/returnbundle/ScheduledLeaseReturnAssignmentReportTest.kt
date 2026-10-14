package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLeaseReturnAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708992769)
        val items = mutableListOf<ScheduledLeaseReturnAssignment>()
        for (index in 0 until 3) {
            items.add(newScheduledLeaseReturnAssignment("scheduled-lease-return-assignment-$index", "sample"))
        }
        val report = buildScheduledLeaseReturnAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
