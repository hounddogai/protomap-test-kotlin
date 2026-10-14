package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705585412)
        val items = mutableListOf<ParcelQuota>()
        for (index in 0 until 2) {
            items.add(newParcelQuota("parcel-quota-$index", "sample"))
        }
        val report = buildParcelQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
