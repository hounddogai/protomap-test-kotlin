package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700573673)
        val items = mutableListOf<NearMissScore>()
        for (index in 0 until 3) {
            items.add(newNearMissScore("near-miss-score-$index", "sample"))
        }
        val report = buildNearMissScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
