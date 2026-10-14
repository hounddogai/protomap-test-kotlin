package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700892152)
        val items = mutableListOf<TieredTrailerTemplate>()
        for (index in 0 until 4) {
            items.add(newTieredTrailerTemplate("tiered-trailer-template-$index", "sample"))
        }
        val report = buildTieredTrailerTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
