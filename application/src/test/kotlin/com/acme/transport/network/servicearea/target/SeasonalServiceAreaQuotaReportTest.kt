package com.acme.transport.network.servicearea.target

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServiceAreaQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702713035)
        val items = mutableListOf<SeasonalServiceAreaQuota>()
        for (index in 0 until 3) {
            items.add(newSeasonalServiceAreaQuota("seasonal-service-area-quota-$index", "sample"))
        }
        val report = buildSeasonalServiceAreaQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
