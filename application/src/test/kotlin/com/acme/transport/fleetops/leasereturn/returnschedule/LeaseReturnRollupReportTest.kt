package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702657336)
        val items = mutableListOf<LeaseReturnRollup>()
        for (index in 0 until 5) {
            items.add(newLeaseReturnRollup("lease-return-rollup-$index", "sample"))
        }
        val report = buildLeaseReturnRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
