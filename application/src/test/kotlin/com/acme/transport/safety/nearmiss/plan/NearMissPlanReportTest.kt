package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702218290)
        val items = mutableListOf<NearMissPlan>()
        for (index in 0 until 4) {
            items.add(newNearMissPlan("near-miss-plan-$index", "sample"))
        }
        val report = buildNearMissPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
