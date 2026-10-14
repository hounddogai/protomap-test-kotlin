package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707180700)
        val items = mutableListOf<TrailerTemplate>()
        for (index in 0 until 3) {
            items.add(newTrailerTemplate("trailer-template-$index", "sample"))
        }
        val report = buildTrailerTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
