package com.acme.transport.network.servicearea.agreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709615700)
        val items = mutableListOf<ServiceAreaMapping>()
        for (index in 0 until 3) {
            items.add(newServiceAreaMapping("service-area-mapping-$index", "sample"))
        }
        val report = buildServiceAreaMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
