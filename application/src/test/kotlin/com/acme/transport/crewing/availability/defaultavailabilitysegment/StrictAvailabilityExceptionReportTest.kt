package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAvailabilityExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700770929)
        val items = mutableListOf<StrictAvailabilityException>()
        for (index in 0 until 6) {
            items.add(newStrictAvailabilityException("strict-availability-exception-$index", "sample"))
        }
        val report = buildStrictAvailabilityExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
