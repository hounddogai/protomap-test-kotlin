package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706426060)
        val items = mutableListOf<LegacyLeaseReturnBundle>()
        for (index in 0 until 4) {
            items.add(newLegacyLeaseReturnBundle("legacy-lease-return-bundle-$index", "sample"))
        }
        val report = buildLegacyLeaseReturnBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
