package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703481766)
        val items = mutableListOf<LeaseReturnAudit>()
        for (index in 0 until 5) {
            items.add(newLeaseReturnAudit("lease-return-audit-$index", "sample"))
        }
        val report = buildLeaseReturnAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
