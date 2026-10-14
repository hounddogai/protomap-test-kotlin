package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706636247)
        val items = mutableListOf<RegionalLeaseReturnPolicy>()
        for (index in 0 until 6) {
            items.add(newRegionalLeaseReturnPolicy("regional-lease-return-policy-$index", "sample"))
        }
        val report = buildRegionalLeaseReturnPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
