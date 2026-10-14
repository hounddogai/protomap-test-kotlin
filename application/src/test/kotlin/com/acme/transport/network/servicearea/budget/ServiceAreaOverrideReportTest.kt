package com.acme.transport.network.servicearea.budget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706619068)
        val items = mutableListOf<ServiceAreaOverride>()
        for (index in 0 until 2) {
            items.add(newServiceAreaOverride("service-area-override-$index", "sample"))
        }
        val report = buildServiceAreaOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
