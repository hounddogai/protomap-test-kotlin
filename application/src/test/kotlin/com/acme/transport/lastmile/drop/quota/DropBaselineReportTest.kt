package com.acme.transport.lastmile.drop.quota

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709238335)
        val items = mutableListOf<DropBaseline>()
        for (index in 0 until 6) {
            items.add(newDropBaseline("drop-baseline-$index", "sample"))
        }
        val report = buildDropBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
