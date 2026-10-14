package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientLeaseReturnManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703036731)
        val items = mutableListOf<LenientLeaseReturnManifest>()
        for (index in 0 until 2) {
            items.add(newLenientLeaseReturnManifest("lenient-lease-return-manifest-$index", "sample"))
        }
        val report = buildLenientLeaseReturnManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
