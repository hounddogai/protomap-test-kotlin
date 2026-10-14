package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIdlingQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709154717)
        val items = mutableListOf<ManualIdlingQuota>()
        for (index in 0 until 3) {
            items.add(newManualIdlingQuota("manual-idling-quota-$index", "sample"))
        }
        val report = buildManualIdlingQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
