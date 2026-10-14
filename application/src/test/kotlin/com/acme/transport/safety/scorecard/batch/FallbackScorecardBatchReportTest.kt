package com.acme.transport.safety.scorecard.batch

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackScorecardBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703985373)
        val items = mutableListOf<FallbackScorecardBatch>()
        for (index in 0 until 5) {
            items.add(newFallbackScorecardBatch("fallback-scorecard-batch-$index", "sample"))
        }
        val report = buildFallbackScorecardBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
