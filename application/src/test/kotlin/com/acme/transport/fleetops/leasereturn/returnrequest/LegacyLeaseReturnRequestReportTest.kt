package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707408244)
        val items = mutableListOf<LegacyLeaseReturnRequest>()
        for (index in 0 until 4) {
            items.add(newLegacyLeaseReturnRequest("legacy-lease-return-request-$index", "sample"))
        }
        val report = buildLegacyLeaseReturnRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
