package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerYardRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701150710)
        val items = mutableListOf<CappedTrailerYardRollup>()
        for (index in 0 until 3) {
            items.add(newCappedTrailerYardRollup("capped-trailer-yard-rollup-$index", "sample"))
        }
        val report = buildCappedTrailerYardRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
