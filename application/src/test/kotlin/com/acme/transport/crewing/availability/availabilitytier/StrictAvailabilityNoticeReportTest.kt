package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAvailabilityNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703283192)
        val items = mutableListOf<StrictAvailabilityNotice>()
        for (index in 0 until 2) {
            items.add(newStrictAvailabilityNotice("strict-availability-notice-$index", "sample"))
        }
        val report = buildStrictAvailabilityNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
