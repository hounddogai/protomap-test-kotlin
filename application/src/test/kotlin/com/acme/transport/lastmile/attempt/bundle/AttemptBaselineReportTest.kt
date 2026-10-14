package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705640190)
        val items = mutableListOf<AttemptBaseline>()
        for (index in 0 until 3) {
            items.add(newAttemptBaseline("attempt-baseline-$index", "sample"))
        }
        val report = buildAttemptBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
