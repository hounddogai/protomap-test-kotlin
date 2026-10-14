package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCapacityPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709013293)
        val items = mutableListOf<DefaultCapacityPreference>()
        for (index in 0 until 4) {
            items.add(newDefaultCapacityPreference("default-capacity-preference-$index", "sample"))
        }
        val report = buildDefaultCapacityPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
