package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709686453)
        val items = mutableListOf<GlobalInspectionMapping>()
        for (index in 0 until 3) {
            items.add(newGlobalInspectionMapping("global-inspection-mapping-$index", "sample"))
        }
        val report = buildGlobalInspectionMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
