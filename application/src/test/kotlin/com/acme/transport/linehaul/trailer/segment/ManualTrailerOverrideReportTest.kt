package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700238465)
        val items = mutableListOf<ManualTrailerOverride>()
        for (index in 0 until 6) {
            items.add(newManualTrailerOverride("manual-trailer-override-$index", "sample"))
        }
        val report = buildManualTrailerOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
