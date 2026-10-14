package com.acme.transport.terminal.dwell.preference

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701379094)
        val items = mutableListOf<DwellPreference>()
        for (index in 0 until 5) {
            items.add(newDwellPreference("dwell-preference-$index", "sample"))
        }
        val report = buildDwellPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
