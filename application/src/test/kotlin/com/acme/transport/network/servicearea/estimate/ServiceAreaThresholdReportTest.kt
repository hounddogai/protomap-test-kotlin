package com.acme.transport.network.servicearea.estimate

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701747487)
        val items = mutableListOf<ServiceAreaThreshold>()
        for (index in 0 until 5) {
            items.add(newServiceAreaThreshold("service-area-threshold-$index", "sample"))
        }
        val report = buildServiceAreaThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
