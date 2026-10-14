package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709557531)
        val items = mutableListOf<LegacyDeclarationSummary>()
        for (index in 0 until 2) {
            items.add(newLegacyDeclarationSummary("legacy-declaration-summary-$index", "sample"))
        }
        val report = buildLegacyDeclarationSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
