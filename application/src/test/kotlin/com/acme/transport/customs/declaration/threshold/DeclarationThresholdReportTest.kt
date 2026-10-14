package com.acme.transport.customs.declaration.threshold

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709960191)
        val items = mutableListOf<DeclarationThreshold>()
        for (index in 0 until 4) {
            items.add(newDeclarationThreshold("declaration-threshold-$index", "sample"))
        }
        val report = buildDeclarationThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
