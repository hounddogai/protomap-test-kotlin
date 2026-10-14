package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703288732)
        val items = mutableListOf<AttemptException>()
        for (index in 0 until 3) {
            items.add(newAttemptException("attempt-exception-$index", "sample"))
        }
        val report = buildAttemptExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
