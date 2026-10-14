package com.acme.transport.fleetops.inspection.review

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702872913)
        val items = mutableListOf<FallbackInspectionPolicy>()
        for (index in 0 until 5) {
            items.add(newFallbackInspectionPolicy("fallback-inspection-policy-$index", "sample"))
        }
        val report = buildFallbackInspectionPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
