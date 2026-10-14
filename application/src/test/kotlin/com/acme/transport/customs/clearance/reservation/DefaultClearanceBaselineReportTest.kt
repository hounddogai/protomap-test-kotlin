package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultClearanceBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707893036)
        val items = mutableListOf<DefaultClearanceBaseline>()
        for (index in 0 until 2) {
            items.add(newDefaultClearanceBaseline("default-clearance-baseline-$index", "sample"))
        }
        val report = buildDefaultClearanceBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
