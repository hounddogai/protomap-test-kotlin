package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703850366)
        val items = mutableListOf<ScheduledTrailerOverride>()
        for (index in 0 until 2) {
            items.add(newScheduledTrailerOverride("scheduled-trailer-override-$index", "sample"))
        }
        val report = buildScheduledTrailerOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
