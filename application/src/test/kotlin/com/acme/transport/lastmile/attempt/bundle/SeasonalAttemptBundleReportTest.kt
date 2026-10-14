package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAttemptBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708277737)
        val items = mutableListOf<SeasonalAttemptBundle>()
        for (index in 0 until 3) {
            items.add(newSeasonalAttemptBundle("seasonal-attempt-bundle-$index", "sample"))
        }
        val report = buildSeasonalAttemptBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
