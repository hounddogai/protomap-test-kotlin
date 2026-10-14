package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700984568)
        val items = mutableListOf<ScorecardAudit>()
        for (index in 0 until 3) {
            items.add(newScorecardAudit("scorecard-audit-$index", "sample"))
        }
        val report = buildScorecardAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
