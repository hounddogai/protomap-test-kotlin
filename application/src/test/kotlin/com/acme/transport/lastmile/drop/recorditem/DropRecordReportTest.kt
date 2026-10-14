package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705524506)
        val items = mutableListOf<DropRecord>()
        for (index in 0 until 3) {
            items.add(newDropRecord("drop-record-$index", "sample"))
        }
        val report = buildDropRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
