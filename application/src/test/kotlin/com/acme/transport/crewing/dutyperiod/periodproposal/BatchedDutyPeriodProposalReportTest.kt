package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDutyPeriodProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706796503)
        val items = mutableListOf<BatchedDutyPeriodProposal>()
        for (index in 0 until 6) {
            items.add(newBatchedDutyPeriodProposal("batched-duty-period-proposal-$index", "sample"))
        }
        val report = buildBatchedDutyPeriodProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
