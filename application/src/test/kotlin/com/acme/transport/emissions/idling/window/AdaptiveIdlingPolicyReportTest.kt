package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveIdlingPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704799099)
        val items = mutableListOf<AdaptiveIdlingPolicy>()
        for (index in 0 until 6) {
            items.add(newAdaptiveIdlingPolicy("adaptive-idling-policy-$index", "sample"))
        }
        val report = buildAdaptiveIdlingPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
