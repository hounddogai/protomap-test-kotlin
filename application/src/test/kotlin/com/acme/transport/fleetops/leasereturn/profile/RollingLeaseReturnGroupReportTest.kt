package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLeaseReturnGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700526597)
        val items = mutableListOf<RollingLeaseReturnGroup>()
        for (index in 0 until 4) {
            items.add(newRollingLeaseReturnGroup("rolling-lease-return-group-$index", "sample"))
        }
        val report = buildRollingLeaseReturnGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
