package com.acme.transport.safety.incident.preference

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIncidentCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701215132)
        val items = mutableListOf<TieredIncidentCycle>()
        for (index in 0 until 2) {
            items.add(newTieredIncidentCycle("tiered-incident-cycle-$index", "sample"))
        }
        val report = buildTieredIncidentCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
