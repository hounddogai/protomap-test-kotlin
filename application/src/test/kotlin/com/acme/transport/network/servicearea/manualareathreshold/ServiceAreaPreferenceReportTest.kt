package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704046215)
        val items = mutableListOf<ServiceAreaPreference>()
        for (index in 0 until 5) {
            items.add(newServiceAreaPreference("service-area-preference-$index", "sample"))
        }
        val report = buildServiceAreaPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
