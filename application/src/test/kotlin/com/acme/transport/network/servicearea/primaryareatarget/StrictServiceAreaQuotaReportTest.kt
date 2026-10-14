package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703433898)
        val items = mutableListOf<StrictServiceAreaQuota>()
        for (index in 0 until 2) {
            items.add(newStrictServiceAreaQuota("strict-service-area-quota-$index", "sample"))
        }
        val report = buildStrictServiceAreaQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
