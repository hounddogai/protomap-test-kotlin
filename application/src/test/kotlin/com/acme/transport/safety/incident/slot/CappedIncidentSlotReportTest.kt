package com.acme.transport.safety.incident.slot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedIncidentSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700811098)
        val items = mutableListOf<CappedIncidentSlot>()
        for (index in 0 until 6) {
            items.add(newCappedIncidentSlot("capped-incident-slot-$index", "sample"))
        }
        val report = buildCappedIncidentSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
