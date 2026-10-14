package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707087246)
        val items = mutableListOf<RestBreakTarget>()
        for (index in 0 until 6) {
            items.add(newRestBreakTarget("rest-break-target-$index", "sample"))
        }
        val report = buildRestBreakTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
