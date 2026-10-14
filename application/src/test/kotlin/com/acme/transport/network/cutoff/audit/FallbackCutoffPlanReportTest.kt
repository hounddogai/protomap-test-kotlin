package com.acme.transport.network.cutoff.audit

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCutoffPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709063937)
        val items = mutableListOf<FallbackCutoffPlan>()
        for (index in 0 until 3) {
            items.add(newFallbackCutoffPlan("fallback-cutoff-plan-$index", "sample"))
        }
        val report = buildFallbackCutoffPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
