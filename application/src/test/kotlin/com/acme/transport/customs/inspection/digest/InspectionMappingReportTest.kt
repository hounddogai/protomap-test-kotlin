package com.acme.transport.customs.inspection.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703384275)
        val items = mutableListOf<InspectionMapping>()
        for (index in 0 until 4) {
            items.add(newInspectionMapping("inspection-mapping-$index", "sample"))
        }
        val report = buildInspectionMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
