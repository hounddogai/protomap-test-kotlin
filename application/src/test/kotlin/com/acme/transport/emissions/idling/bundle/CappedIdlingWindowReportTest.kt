package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedIdlingWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709567438)
        val items = mutableListOf<CappedIdlingWindow>()
        for (index in 0 until 5) {
            items.add(newCappedIdlingWindow("capped-idling-window-$index", "sample"))
        }
        val report = buildCappedIdlingWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
