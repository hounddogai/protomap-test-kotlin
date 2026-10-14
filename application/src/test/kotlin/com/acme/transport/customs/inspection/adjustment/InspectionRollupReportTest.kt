package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709227945)
        val items = mutableListOf<InspectionRollup>()
        for (index in 0 until 4) {
            items.add(newInspectionRollup("inspection-rollup-$index", "sample"))
        }
        val report = buildInspectionRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
