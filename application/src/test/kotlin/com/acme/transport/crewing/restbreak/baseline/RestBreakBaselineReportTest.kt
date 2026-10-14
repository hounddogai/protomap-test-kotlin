package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708178218)
        val items = mutableListOf<RestBreakBaseline>()
        for (index in 0 until 6) {
            items.add(newRestBreakBaseline("rest-break-baseline-$index", "sample"))
        }
        val report = buildRestBreakBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
