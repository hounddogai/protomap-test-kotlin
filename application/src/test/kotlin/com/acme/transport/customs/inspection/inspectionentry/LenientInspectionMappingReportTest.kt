package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708393789)
        val items = mutableListOf<LenientInspectionMapping>()
        for (index in 0 until 2) {
            items.add(newLenientInspectionMapping("lenient-inspection-mapping-$index", "sample"))
        }
        val report = buildLenientInspectionMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
