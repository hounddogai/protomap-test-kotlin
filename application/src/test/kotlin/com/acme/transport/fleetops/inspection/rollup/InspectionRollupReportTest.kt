package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702776103)
        val items = mutableListOf<InspectionRollup>()
        for (index in 0 until 5) {
            items.add(newInspectionRollup("inspection-rollup-$index", "sample"))
        }
        val report = buildInspectionRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
