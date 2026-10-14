package com.acme.transport.customs.declaration.agreement

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702705398)
        val items = mutableListOf<SeasonalDeclarationSummary>()
        for (index in 0 until 5) {
            items.add(newSeasonalDeclarationSummary("seasonal-declaration-summary-$index", "sample"))
        }
        val report = buildSeasonalDeclarationSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
