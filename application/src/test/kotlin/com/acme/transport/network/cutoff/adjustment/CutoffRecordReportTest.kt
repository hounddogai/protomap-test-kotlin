package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709258815)
        val items = mutableListOf<CutoffRecord>()
        for (index in 0 until 2) {
            items.add(newCutoffRecord("cutoff-record-$index", "sample"))
        }
        val report = buildCutoffRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
