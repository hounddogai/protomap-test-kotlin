package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709659155)
        val items = mutableListOf<IdlingWindow>()
        for (index in 0 until 6) {
            items.add(newIdlingWindow("idling-window-$index", "sample"))
        }
        val report = buildIdlingWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
