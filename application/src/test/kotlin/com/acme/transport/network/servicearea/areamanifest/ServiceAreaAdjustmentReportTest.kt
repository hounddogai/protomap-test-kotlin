package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702610155)
        val items = mutableListOf<ServiceAreaAdjustment>()
        for (index in 0 until 2) {
            items.add(newServiceAreaAdjustment("service-area-adjustment-$index", "sample"))
        }
        val report = buildServiceAreaAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
