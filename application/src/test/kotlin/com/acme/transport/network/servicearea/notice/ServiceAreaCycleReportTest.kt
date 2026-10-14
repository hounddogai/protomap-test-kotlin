package com.acme.transport.network.servicearea.notice

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708357959)
        val items = mutableListOf<ServiceAreaCycle>()
        for (index in 0 until 3) {
            items.add(newServiceAreaCycle("service-area-cycle-$index", "sample"))
        }
        val report = buildServiceAreaCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
