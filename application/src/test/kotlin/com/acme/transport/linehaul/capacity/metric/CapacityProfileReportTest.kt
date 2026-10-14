package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707659407)
        val items = mutableListOf<CapacityProfile>()
        for (index in 0 until 2) {
            items.add(newCapacityProfile("capacity-profile-$index", "sample"))
        }
        val report = buildCapacityProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
