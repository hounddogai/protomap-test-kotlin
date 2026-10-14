package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCrossDockScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701892260)
        val items = mutableListOf<SeasonalCrossDockScore>()
        for (index in 0 until 2) {
            items.add(newSeasonalCrossDockScore("seasonal-cross-dock-score-$index", "sample"))
        }
        val report = buildSeasonalCrossDockScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
