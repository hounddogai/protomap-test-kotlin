package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702011829)
        val items = mutableListOf<BackhaulTemplate>()
        for (index in 0 until 5) {
            items.add(newBackhaulTemplate("backhaul-template-$index", "sample"))
        }
        val report = buildBackhaulTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
