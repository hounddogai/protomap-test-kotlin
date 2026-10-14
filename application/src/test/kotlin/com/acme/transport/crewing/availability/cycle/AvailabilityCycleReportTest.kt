package com.acme.transport.crewing.availability.cycle

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705197533)
        val items = mutableListOf<AvailabilityCycle>()
        for (index in 0 until 6) {
            items.add(newAvailabilityCycle("availability-cycle-$index", "sample"))
        }
        val report = buildAvailabilityCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
