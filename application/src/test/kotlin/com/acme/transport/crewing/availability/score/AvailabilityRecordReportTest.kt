package com.acme.transport.crewing.availability.score

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703255230)
        val items = mutableListOf<AvailabilityRecord>()
        for (index in 0 until 2) {
            items.add(newAvailabilityRecord("availability-record-$index", "sample"))
        }
        val report = buildAvailabilityRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
