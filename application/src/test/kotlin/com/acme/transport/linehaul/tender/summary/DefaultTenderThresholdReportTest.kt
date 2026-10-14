package com.acme.transport.linehaul.tender.summary

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTenderThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703746868)
        val items = mutableListOf<DefaultTenderThreshold>()
        for (index in 0 until 6) {
            items.add(newDefaultTenderThreshold("default-tender-threshold-$index", "sample"))
        }
        val report = buildDefaultTenderThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
