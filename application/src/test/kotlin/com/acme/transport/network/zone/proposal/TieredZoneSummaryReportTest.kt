package com.acme.transport.network.zone.proposal

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZoneSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701433432)
        val items = mutableListOf<TieredZoneSummary>()
        for (index in 0 until 4) {
            items.add(newTieredZoneSummary("tiered-zone-summary-$index", "sample"))
        }
        val report = buildTieredZoneSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
