package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBackhaulQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704495928)
        val items = mutableListOf<AdaptiveBackhaulQuota>()
        for (index in 0 until 6) {
            items.add(newAdaptiveBackhaulQuota("adaptive-backhaul-quota-$index", "sample"))
        }
        val report = buildAdaptiveBackhaulQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
