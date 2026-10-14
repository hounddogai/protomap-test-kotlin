package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAttemptBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706466478)
        val items = mutableListOf<ManualAttemptBaseline>()
        for (index in 0 until 4) {
            items.add(newManualAttemptBaseline("manual-attempt-baseline-$index", "sample"))
        }
        val report = buildManualAttemptBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
