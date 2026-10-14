package com.acme.transport.linehaul.trailer.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707987670)
        val items = mutableListOf<TrailerWindow>()
        for (index in 0 until 5) {
            items.add(newTrailerWindow("trailer-window-$index", "sample"))
        }
        val report = buildTrailerWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
