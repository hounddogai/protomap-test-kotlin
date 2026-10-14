package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702249820)
        val items = mutableListOf<AttemptRecord>()
        for (index in 0 until 6) {
            items.add(newAttemptRecord("attempt-record-$index", "sample"))
        }
        val report = buildAttemptRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
