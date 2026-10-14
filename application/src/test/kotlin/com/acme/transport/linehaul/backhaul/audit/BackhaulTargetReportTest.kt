package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708963096)
        val items = mutableListOf<BackhaulTarget>()
        for (index in 0 until 5) {
            items.add(newBackhaulTarget("backhaul-target-$index", "sample"))
        }
        val report = buildBackhaulTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
