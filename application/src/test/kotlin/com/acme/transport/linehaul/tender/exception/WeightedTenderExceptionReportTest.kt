package com.acme.transport.linehaul.tender.exception

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTenderExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707359397)
        val items = mutableListOf<WeightedTenderException>()
        for (index in 0 until 6) {
            items.add(newWeightedTenderException("weighted-tender-exception-$index", "sample"))
        }
        val report = buildWeightedTenderExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
