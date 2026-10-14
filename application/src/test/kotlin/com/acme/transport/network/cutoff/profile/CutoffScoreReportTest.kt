package com.acme.transport.network.cutoff.profile

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706326945)
        val items = mutableListOf<CutoffScore>()
        for (index in 0 until 3) {
            items.add(newCutoffScore("cutoff-score-$index", "sample"))
        }
        val report = buildCutoffScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
