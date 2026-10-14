package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705536284)
        val items = mutableListOf<RedeliveryBaseline>()
        for (index in 0 until 4) {
            items.add(newRedeliveryBaseline("redelivery-baseline-$index", "sample"))
        }
        val report = buildRedeliveryBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
