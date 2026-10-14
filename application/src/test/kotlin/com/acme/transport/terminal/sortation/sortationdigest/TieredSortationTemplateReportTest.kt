package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702123865)
        val items = mutableListOf<TieredSortationTemplate>()
        for (index in 0 until 6) {
            items.add(newTieredSortationTemplate("tiered-sortation-template-$index", "sample"))
        }
        val report = buildTieredSortationTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
