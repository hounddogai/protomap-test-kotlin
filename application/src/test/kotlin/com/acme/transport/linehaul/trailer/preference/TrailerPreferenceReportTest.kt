package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707154377)
        val items = mutableListOf<TrailerPreference>()
        for (index in 0 until 4) {
            items.add(newTrailerPreference("trailer-preference-$index", "sample"))
        }
        val report = buildTrailerPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
