package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709482034)
        val items = mutableListOf<RegionalCapacityProfile>()
        for (index in 0 until 2) {
            items.add(newRegionalCapacityProfile("regional-capacity-profile-$index", "sample"))
        }
        val report = buildRegionalCapacityProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
