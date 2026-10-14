package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708805251)
        val items = mutableListOf<CappedTrailerThreshold>()
        for (index in 0 until 2) {
            items.add(newCappedTrailerThreshold("capped-trailer-threshold-$index", "sample"))
        }
        val report = buildCappedTrailerThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
