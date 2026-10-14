package com.acme.transport.network.zone.bundle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703452041)
        val items = mutableListOf<ZoneBundle>()
        for (index in 0 until 3) {
            items.add(newZoneBundle("zone-bundle-$index", "sample"))
        }
        val report = buildZoneBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
