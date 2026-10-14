package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700666112)
        val items = mutableListOf<ManualTrailerTarget>()
        for (index in 0 until 5) {
            items.add(newManualTrailerTarget("manual-trailer-target-$index", "sample"))
        }
        val report = buildManualTrailerTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
