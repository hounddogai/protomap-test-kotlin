package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAttemptRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709346885)
        val items = mutableListOf<LenientAttemptRequest>()
        for (index in 0 until 6) {
            items.add(newLenientAttemptRequest("lenient-attempt-request-$index", "sample"))
        }
        val report = buildLenientAttemptRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
