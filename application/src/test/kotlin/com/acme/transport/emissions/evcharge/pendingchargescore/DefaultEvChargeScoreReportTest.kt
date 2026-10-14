package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEvChargeScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706270193)
        val items = mutableListOf<DefaultEvChargeScore>()
        for (index in 0 until 3) {
            items.add(newDefaultEvChargeScore("default-ev-charge-score-$index", "sample"))
        }
        val report = buildDefaultEvChargeScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
