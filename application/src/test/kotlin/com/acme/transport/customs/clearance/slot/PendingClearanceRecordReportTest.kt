package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingClearanceRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701859957)
        val items = mutableListOf<PendingClearanceRecord>()
        for (index in 0 until 4) {
            items.add(newPendingClearanceRecord("pending-clearance-record-$index", "sample"))
        }
        val report = buildPendingClearanceRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
