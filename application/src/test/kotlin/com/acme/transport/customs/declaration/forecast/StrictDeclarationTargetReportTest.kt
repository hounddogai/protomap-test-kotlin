package com.acme.transport.customs.declaration.forecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDeclarationTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704593445)
        val items = mutableListOf<StrictDeclarationTarget>()
        for (index in 0 until 6) {
            items.add(newStrictDeclarationTarget("strict-declaration-target-$index", "sample"))
        }
        val report = buildStrictDeclarationTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
