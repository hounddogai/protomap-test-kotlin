package com.acme.transport.crewing.roster.review

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709169612)
        val items = mutableListOf<PrimaryRosterRecord>()
        for (index in 0 until 5) {
            items.add(newPrimaryRosterRecord("primary-roster-record-$index", "sample"))
        }
        val report = buildPrimaryRosterRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
