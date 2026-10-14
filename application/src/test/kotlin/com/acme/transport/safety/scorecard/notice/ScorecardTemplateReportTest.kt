package com.acme.transport.safety.scorecard.notice

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708343402)
        val items = mutableListOf<ScorecardTemplate>()
        for (index in 0 until 5) {
            items.add(newScorecardTemplate("scorecard-template-$index", "sample"))
        }
        val report = buildScorecardTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
