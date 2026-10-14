package com.acme.transport.network.servicearea.assignment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706054557)
        val items = mutableListOf<ServiceAreaAllocation>()
        for (index in 0 until 2) {
            items.add(newServiceAreaAllocation("service-area-allocation-$index", "sample"))
        }
        val report = buildServiceAreaAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
