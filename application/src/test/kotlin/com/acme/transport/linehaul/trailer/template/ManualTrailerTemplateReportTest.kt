package com.acme.transport.linehaul.trailer.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700705569)
        val items = mutableListOf<ManualTrailerTemplate>()
        for (index in 0 until 3) {
            items.add(newManualTrailerTemplate("manual-trailer-template-$index", "sample"))
        }
        val report = buildManualTrailerTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
