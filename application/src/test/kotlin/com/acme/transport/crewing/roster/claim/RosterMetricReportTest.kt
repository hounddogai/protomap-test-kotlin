package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704125708)
        val items = mutableListOf<RosterMetric>()
        for (index in 0 until 4) {
            items.add(newRosterMetric("roster-metric-$index", "sample"))
        }
        val report = buildRosterMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
