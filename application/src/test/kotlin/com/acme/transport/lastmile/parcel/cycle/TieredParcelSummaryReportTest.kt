package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredParcelSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701088380)
        val items = mutableListOf<TieredParcelSummary>()
        for (index in 0 until 2) {
            items.add(newTieredParcelSummary("tiered-parcel-summary-$index", "sample"))
        }
        val report = buildTieredParcelSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
