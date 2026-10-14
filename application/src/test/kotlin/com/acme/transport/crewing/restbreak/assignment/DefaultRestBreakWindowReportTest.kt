package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRestBreakWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703915092)
        val items = mutableListOf<DefaultRestBreakWindow>()
        for (index in 0 until 4) {
            items.add(newDefaultRestBreakWindow("default-rest-break-window-$index", "sample"))
        }
        val report = buildDefaultRestBreakWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
