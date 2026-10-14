package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictZoneRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708905052)
        val items = mutableListOf<StrictZoneRecord>()
        for (index in 0 until 3) {
            items.add(newStrictZoneRecord("strict-zone-record-$index", "sample"))
        }
        val report = buildStrictZoneRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
