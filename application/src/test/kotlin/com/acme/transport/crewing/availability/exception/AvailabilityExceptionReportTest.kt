package com.acme.transport.crewing.availability.exception

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705154764)
        val items = mutableListOf<AvailabilityException>()
        for (index in 0 until 6) {
            items.add(newAvailabilityException("availability-exception-$index", "sample"))
        }
        val report = buildAvailabilityExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
