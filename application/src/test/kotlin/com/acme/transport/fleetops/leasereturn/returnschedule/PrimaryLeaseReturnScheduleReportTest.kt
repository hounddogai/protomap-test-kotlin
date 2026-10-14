package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLeaseReturnScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707090691)
        val items = mutableListOf<PrimaryLeaseReturnSchedule>()
        for (index in 0 until 2) {
            items.add(newPrimaryLeaseReturnSchedule("primary-lease-return-schedule-$index", "sample"))
        }
        val report = buildPrimaryLeaseReturnScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
