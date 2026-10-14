package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706261252)
        val items = mutableListOf<FallbackDeclarationTarget>()
        for (index in 0 until 4) {
            items.add(newFallbackDeclarationTarget("fallback-declaration-target-$index", "sample"))
        }
        val report = buildFallbackDeclarationTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
