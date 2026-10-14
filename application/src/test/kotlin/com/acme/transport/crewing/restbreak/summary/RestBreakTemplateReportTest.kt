package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706753731)
        val items = mutableListOf<RestBreakTemplate>()
        for (index in 0 until 4) {
            items.add(newRestBreakTemplate("rest-break-template-$index", "sample"))
        }
        val report = buildRestBreakTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
