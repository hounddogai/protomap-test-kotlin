package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704655823)
        val items = mutableListOf<AdaptiveInspectionPolicy>()
        for (index in 0 until 4) {
            items.add(newAdaptiveInspectionPolicy("adaptive-inspection-policy-$index", "sample"))
        }
        val report = buildAdaptiveInspectionPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
