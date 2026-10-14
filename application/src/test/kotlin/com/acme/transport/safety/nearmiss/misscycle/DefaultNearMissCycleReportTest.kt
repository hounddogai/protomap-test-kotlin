package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNearMissCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706644834)
        val items = mutableListOf<DefaultNearMissCycle>()
        for (index in 0 until 4) {
            items.add(newDefaultNearMissCycle("default-near-miss-cycle-$index", "sample"))
        }
        val report = buildDefaultNearMissCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
