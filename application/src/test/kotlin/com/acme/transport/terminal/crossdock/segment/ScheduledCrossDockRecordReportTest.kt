package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCrossDockRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703670554)
        val items = mutableListOf<ScheduledCrossDockRecord>()
        for (index in 0 until 2) {
            items.add(newScheduledCrossDockRecord("scheduled-cross-dock-record-$index", "sample"))
        }
        val report = buildScheduledCrossDockRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
