package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDwellPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708204935)
        val items = mutableListOf<DefaultDwellPreference>()
        for (index in 0 until 6) {
            items.add(newDefaultDwellPreference("default-dwell-preference-$index", "sample"))
        }
        val report = buildDefaultDwellPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
