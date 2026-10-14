package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLeaseReturnScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706424508)
        val items = mutableListOf<AdaptiveLeaseReturnSchedule>()
        for (index in 0 until 3) {
            items.add(newAdaptiveLeaseReturnSchedule("adaptive-lease-return-schedule-$index", "sample"))
        }
        val report = buildAdaptiveLeaseReturnScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
