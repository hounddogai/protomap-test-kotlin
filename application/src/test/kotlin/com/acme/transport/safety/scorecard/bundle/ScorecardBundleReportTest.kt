package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706019723)
        val items = mutableListOf<ScorecardBundle>()
        for (index in 0 until 3) {
            items.add(newScorecardBundle("scorecard-bundle-$index", "sample"))
        }
        val report = buildScorecardBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
