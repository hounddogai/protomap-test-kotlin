package com.acme.transport.network.zone.overrideitem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701614803)
        val items = mutableListOf<LegacyZoneOverride>()
        for (index in 0 until 2) {
            items.add(newLegacyZoneOverride("legacy-zone-override-$index", "sample"))
        }
        val report = buildLegacyZoneOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
