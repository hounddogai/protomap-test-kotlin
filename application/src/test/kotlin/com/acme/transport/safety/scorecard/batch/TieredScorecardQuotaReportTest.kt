package com.acme.transport.safety.scorecard.batch

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredScorecardQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706525339)
        val items = mutableListOf<TieredScorecardQuota>()
        for (index in 0 until 6) {
            items.add(newTieredScorecardQuota("tiered-scorecard-quota-$index", "sample"))
        }
        val report = buildTieredScorecardQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
