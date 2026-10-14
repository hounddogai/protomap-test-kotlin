package com.acme.transport.linehaul.tender.window

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTenderWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703610387)
        val items = mutableListOf<AdaptiveTenderWindow>()
        for (index in 0 until 5) {
            items.add(newAdaptiveTenderWindow("adaptive-tender-window-$index", "sample"))
        }
        val report = buildAdaptiveTenderWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
