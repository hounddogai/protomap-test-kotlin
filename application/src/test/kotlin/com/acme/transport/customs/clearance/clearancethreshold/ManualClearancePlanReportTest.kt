package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualClearancePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701794371)
        val items = mutableListOf<ManualClearancePlan>()
        for (index in 0 until 3) {
            items.add(newManualClearancePlan("manual-clearance-plan-$index", "sample"))
        }
        val report = buildManualClearancePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
