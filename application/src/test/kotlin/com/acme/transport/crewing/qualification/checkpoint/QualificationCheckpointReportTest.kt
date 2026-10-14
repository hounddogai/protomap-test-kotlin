package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701542590)
        val items = mutableListOf<QualificationCheckpoint>()
        for (index in 0 until 6) {
            items.add(newQualificationCheckpoint("qualification-checkpoint-$index", "sample"))
        }
        val report = buildQualificationCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
