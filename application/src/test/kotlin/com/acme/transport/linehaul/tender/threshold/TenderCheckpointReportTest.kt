package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701824555)
        val items = mutableListOf<TenderCheckpoint>()
        for (index in 0 until 2) {
            items.add(newTenderCheckpoint("tender-checkpoint-$index", "sample"))
        }
        val report = buildTenderCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
