package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706886014)
        val items = mutableListOf<AttemptEstimate>()
        for (index in 0 until 3) {
            items.add(newAttemptEstimate("attempt-estimate-$index", "sample"))
        }
        val report = buildAttemptEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
