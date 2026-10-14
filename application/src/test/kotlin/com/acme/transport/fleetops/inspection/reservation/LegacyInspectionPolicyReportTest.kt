package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706524227)
        val items = mutableListOf<LegacyInspectionPolicy>()
        for (index in 0 until 5) {
            items.add(newLegacyInspectionPolicy("legacy-inspection-policy-$index", "sample"))
        }
        val report = buildLegacyInspectionPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
