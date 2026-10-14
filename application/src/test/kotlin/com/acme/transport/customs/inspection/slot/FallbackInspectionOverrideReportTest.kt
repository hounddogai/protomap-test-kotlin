package com.acme.transport.customs.inspection.slot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707630947)
        val items = mutableListOf<FallbackInspectionOverride>()
        for (index in 0 until 3) {
            items.add(newFallbackInspectionOverride("fallback-inspection-override-$index", "sample"))
        }
        val report = buildFallbackInspectionOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
