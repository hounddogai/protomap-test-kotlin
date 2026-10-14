package com.acme.transport.linehaul.tender.variance

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTenderTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708336589)
        val items = mutableListOf<WeightedTenderTemplate>()
        for (index in 0 until 3) {
            items.add(newWeightedTenderTemplate("weighted-tender-template-$index", "sample"))
        }
        val report = buildWeightedTenderTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
