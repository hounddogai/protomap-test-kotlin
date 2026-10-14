package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702172308)
        val items = mutableListOf<RegionalParcelVariance>()
        for (index in 0 until 4) {
            items.add(newRegionalParcelVariance("regional-parcel-variance-$index", "sample"))
        }
        val report = buildRegionalParcelVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
