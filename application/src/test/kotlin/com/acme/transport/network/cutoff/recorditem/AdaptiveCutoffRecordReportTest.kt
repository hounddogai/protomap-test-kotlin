package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCutoffRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701375371)
        val items = mutableListOf<AdaptiveCutoffRecord>()
        for (index in 0 until 3) {
            items.add(newAdaptiveCutoffRecord("adaptive-cutoff-record-$index", "sample"))
        }
        val report = buildAdaptiveCutoffRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
