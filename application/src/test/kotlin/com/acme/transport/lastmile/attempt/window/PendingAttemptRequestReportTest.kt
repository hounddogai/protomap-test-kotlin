package com.acme.transport.lastmile.attempt.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAttemptRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705800008)
        val items = mutableListOf<PendingAttemptRequest>()
        for (index in 0 until 3) {
            items.add(newPendingAttemptRequest("pending-attempt-request-$index", "sample"))
        }
        val report = buildPendingAttemptRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
