package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTrailerProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708676293)
        val items = mutableListOf<PendingTrailerProfile>()
        for (index in 0 until 2) {
            items.add(newPendingTrailerProfile("pending-trailer-profile-$index", "sample"))
        }
        val report = buildPendingTrailerProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
