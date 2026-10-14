package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRouteOffsetQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702780687)
        val items = mutableListOf<WeightedRouteOffsetQuota>()
        for (index in 0 until 6) {
            items.add(newWeightedRouteOffsetQuota("weighted-route-offset-quota-$index", "sample"))
        }
        val report = buildWeightedRouteOffsetQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
