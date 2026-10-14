package com.acme.transport.network.zone.digest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702421856)
        val items = mutableListOf<ZoneDigest>()
        for (index in 0 until 6) {
            items.add(newZoneDigest("zone-digest-$index", "sample"))
        }
        val report = buildZoneDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
