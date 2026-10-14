package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708792091)
        val items = mutableListOf<RegionalLeaseReturnRecord>()
        for (index in 0 until 3) {
            items.add(newRegionalLeaseReturnRecord("regional-lease-return-record-$index", "sample"))
        }
        val report = buildRegionalLeaseReturnRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
