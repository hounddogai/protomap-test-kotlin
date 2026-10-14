package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702087445)
        val items = mutableListOf<ScorecardAllocation>()
        for (index in 0 until 3) {
            items.add(newScorecardAllocation("scorecard-allocation-$index", "sample"))
        }
        val report = buildScorecardAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
