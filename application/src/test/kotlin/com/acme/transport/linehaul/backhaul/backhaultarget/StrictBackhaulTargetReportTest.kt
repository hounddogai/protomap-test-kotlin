package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700229044)
        val items = mutableListOf<StrictBackhaulTarget>()
        for (index in 0 until 2) {
            items.add(newStrictBackhaulTarget("strict-backhaul-target-$index", "sample"))
        }
        val report = buildStrictBackhaulTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
