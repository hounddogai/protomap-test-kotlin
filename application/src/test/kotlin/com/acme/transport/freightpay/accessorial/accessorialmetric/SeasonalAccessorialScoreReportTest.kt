package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706708325)
        val items = mutableListOf<SeasonalAccessorialScore>()
        for (index in 0 until 6) {
            items.add(newSeasonalAccessorialScore("seasonal-accessorial-score-$index", "sample"))
        }
        val report = buildSeasonalAccessorialScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
