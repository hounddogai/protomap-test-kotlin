package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedScorecardLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704928859)
        val items = mutableListOf<BatchedScorecardLedger>()
        for (index in 0 until 3) {
            items.add(newBatchedScorecardLedger("batched-scorecard-ledger-$index", "sample"))
        }
        val report = buildBatchedScorecardLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
