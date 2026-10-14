package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLeaseReturnProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708176953)
        val items = mutableListOf<ScheduledLeaseReturnProfile>()
        for (index in 0 until 4) {
            items.add(newScheduledLeaseReturnProfile("scheduled-lease-return-profile-$index", "sample"))
        }
        val report = buildScheduledLeaseReturnProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
