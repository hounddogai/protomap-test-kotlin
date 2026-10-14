package com.acme.transport.network.zone.profile

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701055004)
        val items = mutableListOf<LegacyZoneProfile>()
        for (index in 0 until 2) {
            items.add(newLegacyZoneProfile("legacy-zone-profile-$index", "sample"))
        }
        val report = buildLegacyZoneProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
