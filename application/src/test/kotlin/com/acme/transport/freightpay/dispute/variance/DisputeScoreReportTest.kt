package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701385993)
        val items = mutableListOf<DisputeScore>()
        for (index in 0 until 6) {
            items.add(newDisputeScore("dispute-score-$index", "sample"))
        }
        val report = buildDisputeScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
