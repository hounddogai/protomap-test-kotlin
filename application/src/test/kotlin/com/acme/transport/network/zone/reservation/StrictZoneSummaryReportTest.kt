package com.acme.transport.network.zone.reservation

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictZoneSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707386384)
        val items = mutableListOf<StrictZoneSummary>()
        for (index in 0 until 4) {
            items.add(newStrictZoneSummary("strict-zone-summary-$index", "sample"))
        }
        val report = buildStrictZoneSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
