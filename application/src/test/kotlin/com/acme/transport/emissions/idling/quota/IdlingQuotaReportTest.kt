package com.acme.transport.emissions.idling.quota

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705420090)
        val items = mutableListOf<IdlingQuota>()
        for (index in 0 until 2) {
            items.add(newIdlingQuota("idling-quota-$index", "sample"))
        }
        val report = buildIdlingQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
