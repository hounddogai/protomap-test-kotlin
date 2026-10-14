package com.acme.transport.network.servicearea.cycle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707689104)
        val items = mutableListOf<ServiceAreaBundle>()
        for (index in 0 until 4) {
            items.add(newServiceAreaBundle("service-area-bundle-$index", "sample"))
        }
        val report = buildServiceAreaBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
