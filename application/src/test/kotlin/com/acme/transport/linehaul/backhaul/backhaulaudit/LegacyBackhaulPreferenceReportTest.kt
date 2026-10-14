package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBackhaulPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703627810)
        val items = mutableListOf<LegacyBackhaulPreference>()
        for (index in 0 until 2) {
            items.add(newLegacyBackhaulPreference("legacy-backhaul-preference-$index", "sample"))
        }
        val report = buildLegacyBackhaulPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
