package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAttemptRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701652393)
        val items = mutableListOf<CappedAttemptRecord>()
        for (index in 0 until 3) {
            items.add(newCappedAttemptRecord("capped-attempt-record-$index", "sample"))
        }
        val report = buildCappedAttemptRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
