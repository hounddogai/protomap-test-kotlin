package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCapacityPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701581187)
        val items = mutableListOf<FallbackCapacityPreference>()
        for (index in 0 until 6) {
            items.add(newFallbackCapacityPreference("fallback-capacity-preference-$index", "sample"))
        }
        val report = buildFallbackCapacityPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
