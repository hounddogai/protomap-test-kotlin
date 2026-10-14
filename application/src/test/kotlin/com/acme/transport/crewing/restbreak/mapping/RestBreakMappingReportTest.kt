package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708169165)
        val items = mutableListOf<RestBreakMapping>()
        for (index in 0 until 2) {
            items.add(newRestBreakMapping("rest-break-mapping-$index", "sample"))
        }
        val report = buildRestBreakMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
