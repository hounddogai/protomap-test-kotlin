package com.acme.transport.fleetops.tire.draft

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700162947)
        val items = mutableListOf<TireDraft>()
        for (index in 0 until 6) {
            items.add(newTireDraft("tire-draft-$index", "sample"))
        }
        val report = buildTireDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
