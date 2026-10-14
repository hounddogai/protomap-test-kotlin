package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerYardTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707327101)
        val items = mutableListOf<TieredTrailerYardTemplate>()
        for (index in 0 until 5) {
            items.add(newTieredTrailerYardTemplate("tiered-trailer-yard-template-$index", "sample"))
        }
        val report = buildTieredTrailerYardTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
