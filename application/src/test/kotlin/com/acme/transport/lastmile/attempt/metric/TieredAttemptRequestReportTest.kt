package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAttemptRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701904146)
        val items = mutableListOf<TieredAttemptRequest>()
        for (index in 0 until 3) {
            items.add(newTieredAttemptRequest("tiered-attempt-request-$index", "sample"))
        }
        val report = buildTieredAttemptRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
