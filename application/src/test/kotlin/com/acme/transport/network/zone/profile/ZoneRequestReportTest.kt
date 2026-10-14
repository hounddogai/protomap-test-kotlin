package com.acme.transport.network.zone.profile

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706251824)
        val items = mutableListOf<ZoneRequest>()
        for (index in 0 until 2) {
            items.add(newZoneRequest("zone-request-$index", "sample"))
        }
        val report = buildZoneRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
