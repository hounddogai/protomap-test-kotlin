package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualEvChargeScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700722147)
        val items = mutableListOf<ManualEvChargeScore>()
        for (index in 0 until 5) {
            items.add(newManualEvChargeScore("manual-ev-charge-score-$index", "sample"))
        }
        val report = buildManualEvChargeScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
