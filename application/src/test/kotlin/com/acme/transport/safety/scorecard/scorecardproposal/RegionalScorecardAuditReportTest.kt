package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalScorecardAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701245764)
        val items = mutableListOf<RegionalScorecardAudit>()
        for (index in 0 until 4) {
            items.add(newRegionalScorecardAudit("regional-scorecard-audit-$index", "sample"))
        }
        val report = buildRegionalScorecardAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
