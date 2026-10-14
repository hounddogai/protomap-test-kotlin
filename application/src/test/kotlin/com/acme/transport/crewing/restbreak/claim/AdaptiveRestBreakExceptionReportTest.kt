package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRestBreakExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706376430)
        val items = mutableListOf<AdaptiveRestBreakException>()
        for (index in 0 until 4) {
            items.add(newAdaptiveRestBreakException("adaptive-rest-break-exception-$index", "sample"))
        }
        val report = buildAdaptiveRestBreakExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
