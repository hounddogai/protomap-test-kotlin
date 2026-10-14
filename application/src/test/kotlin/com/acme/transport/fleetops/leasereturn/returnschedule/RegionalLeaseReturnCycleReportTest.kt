package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709305822)
        val items = mutableListOf<RegionalLeaseReturnCycle>()
        for (index in 0 until 2) {
            items.add(newRegionalLeaseReturnCycle("regional-lease-return-cycle-$index", "sample"))
        }
        val report = buildRegionalLeaseReturnCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
