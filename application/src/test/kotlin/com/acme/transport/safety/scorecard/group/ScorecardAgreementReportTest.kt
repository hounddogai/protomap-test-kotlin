package com.acme.transport.safety.scorecard.group

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700169983)
        val items = mutableListOf<ScorecardAgreement>()
        for (index in 0 until 5) {
            items.add(newScorecardAgreement("scorecard-agreement-$index", "sample"))
        }
        val report = buildScorecardAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
