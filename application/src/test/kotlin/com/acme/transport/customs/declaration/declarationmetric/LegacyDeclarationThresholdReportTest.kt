package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704762336)
        val items = mutableListOf<LegacyDeclarationThreshold>()
        for (index in 0 until 2) {
            items.add(newLegacyDeclarationThreshold("legacy-declaration-threshold-$index", "sample"))
        }
        val report = buildLegacyDeclarationThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
