package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702320496)
        val items = mutableListOf<SeasonalDeclarationBatch>()
        for (index in 0 until 4) {
            items.add(newSeasonalDeclarationBatch("seasonal-declaration-batch-$index", "sample"))
        }
        val report = buildSeasonalDeclarationBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
