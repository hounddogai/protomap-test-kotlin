package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRestBreakVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708432668)
        val items = mutableListOf<DefaultRestBreakVariance>()
        for (index in 0 until 2) {
            items.add(newDefaultRestBreakVariance("default-rest-break-variance-$index", "sample"))
        }
        val report = buildDefaultRestBreakVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
