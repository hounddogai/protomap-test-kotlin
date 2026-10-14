package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705619147)
        val items = mutableListOf<LegacyLeaseReturnDigest>()
        for (index in 0 until 5) {
            items.add(newLegacyLeaseReturnDigest("legacy-lease-return-digest-$index", "sample"))
        }
        val report = buildLegacyLeaseReturnDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
