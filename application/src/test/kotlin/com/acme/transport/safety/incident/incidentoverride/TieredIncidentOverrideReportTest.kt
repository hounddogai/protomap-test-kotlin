package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIncidentOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702761436)
        val items = mutableListOf<TieredIncidentOverride>()
        for (index in 0 until 6) {
            items.add(newTieredIncidentOverride("tiered-incident-override-$index", "sample"))
        }
        val report = buildTieredIncidentOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
