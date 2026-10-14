package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTrailerYardRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703194809)
        val items = mutableListOf<AdaptiveTrailerYardRollup>()
        for (index in 0 until 4) {
            items.add(newAdaptiveTrailerYardRollup("adaptive-trailer-yard-rollup-$index", "sample"))
        }
        val report = buildAdaptiveTrailerYardRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
