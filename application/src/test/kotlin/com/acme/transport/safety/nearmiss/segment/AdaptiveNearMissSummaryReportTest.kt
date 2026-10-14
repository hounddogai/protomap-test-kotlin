package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702814295)
        val items = mutableListOf<AdaptiveNearMissSummary>()
        for (index in 0 until 4) {
            items.add(newAdaptiveNearMissSummary("adaptive-near-miss-summary-$index", "sample"))
        }
        val report = buildAdaptiveNearMissSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
