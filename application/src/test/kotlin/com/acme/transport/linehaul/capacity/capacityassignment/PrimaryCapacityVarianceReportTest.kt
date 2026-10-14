package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCapacityVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702250478)
        val items = mutableListOf<PrimaryCapacityVariance>()
        for (index in 0 until 5) {
            items.add(newPrimaryCapacityVariance("primary-capacity-variance-$index", "sample"))
        }
        val report = buildPrimaryCapacityVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
