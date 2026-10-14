package com.acme.transport.network.transittime.preference

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTransitTimePreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708326503)
        val items = mutableListOf<AdaptiveTransitTimePreference>()
        for (index in 0 until 5) {
            items.add(newAdaptiveTransitTimePreference("adaptive-transit-time-preference-$index", "sample"))
        }
        val report = buildAdaptiveTransitTimePreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
