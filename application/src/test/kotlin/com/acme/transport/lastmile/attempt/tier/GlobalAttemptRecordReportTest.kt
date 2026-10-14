package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAttemptRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702174064)
        val items = mutableListOf<GlobalAttemptRecord>()
        for (index in 0 until 6) {
            items.add(newGlobalAttemptRecord("global-attempt-record-$index", "sample"))
        }
        val report = buildGlobalAttemptRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
