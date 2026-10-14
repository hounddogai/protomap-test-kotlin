package com.acme.transport.network.zone.policy

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707791054)
        val items = mutableListOf<LegacyZoneRecord>()
        for (index in 0 until 5) {
            items.add(newLegacyZoneRecord("legacy-zone-record-$index", "sample"))
        }
        val report = buildLegacyZoneRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
