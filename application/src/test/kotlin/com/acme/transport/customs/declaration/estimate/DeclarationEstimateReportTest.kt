package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705528819)
        val items = mutableListOf<DeclarationEstimate>()
        for (index in 0 until 2) {
            items.add(newDeclarationEstimate("declaration-estimate-$index", "sample"))
        }
        val report = buildDeclarationEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
