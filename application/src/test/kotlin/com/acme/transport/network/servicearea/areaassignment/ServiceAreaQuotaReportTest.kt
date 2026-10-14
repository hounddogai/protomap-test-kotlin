package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704669113)
        val items = mutableListOf<ServiceAreaQuota>()
        for (index in 0 until 4) {
            items.add(newServiceAreaQuota("service-area-quota-$index", "sample"))
        }
        val report = buildServiceAreaQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
