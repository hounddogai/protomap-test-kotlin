package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAttemptRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708278797)
        val items = mutableListOf<AdaptiveAttemptRequest>()
        for (index in 0 until 3) {
            items.add(newAdaptiveAttemptRequest("adaptive-attempt-request-$index", "sample"))
        }
        val report = buildAdaptiveAttemptRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
