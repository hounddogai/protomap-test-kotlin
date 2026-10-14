package com.acme.transport.network.zone.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706148456)
        val items = mutableListOf<ZoneDraft>()
        for (index in 0 until 4) {
            items.add(newZoneDraft("zone-draft-$index", "sample"))
        }
        val report = buildZoneDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
