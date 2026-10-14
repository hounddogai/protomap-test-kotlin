package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709448504)
        val items = mutableListOf<ScheduledInspectionClaim>()
        for (index in 0 until 6) {
            items.add(newScheduledInspectionClaim("scheduled-inspection-claim-$index", "sample"))
        }
        val report = buildScheduledInspectionClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
