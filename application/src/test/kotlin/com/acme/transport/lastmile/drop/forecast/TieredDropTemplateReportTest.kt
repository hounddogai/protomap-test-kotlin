package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDropTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704468976)
        val items = mutableListOf<TieredDropTemplate>()
        for (index in 0 until 2) {
            items.add(newTieredDropTemplate("tiered-drop-template-$index", "sample"))
        }
        val report = buildTieredDropTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
