package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientEvChargePreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701992922)
        val items = mutableListOf<LenientEvChargePreference>()
        for (index in 0 until 3) {
            items.add(newLenientEvChargePreference("lenient-ev-charge-preference-$index", "sample"))
        }
        val report = buildLenientEvChargePreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
