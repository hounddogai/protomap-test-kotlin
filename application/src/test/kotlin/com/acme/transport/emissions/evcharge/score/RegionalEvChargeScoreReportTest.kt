package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalEvChargeScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706780616)
        val items = mutableListOf<RegionalEvChargeScore>()
        for (index in 0 until 4) {
            items.add(newRegionalEvChargeScore("regional-ev-charge-score-$index", "sample"))
        }
        val report = buildRegionalEvChargeScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
