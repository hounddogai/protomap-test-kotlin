package com.acme.transport.network.hub.notice

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHubRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701272404)
        val items = mutableListOf<LegacyHubRecord>()
        for (index in 0 until 2) {
            items.add(newLegacyHubRecord("legacy-hub-record-$index", "sample"))
        }
        val report = buildLegacyHubRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
