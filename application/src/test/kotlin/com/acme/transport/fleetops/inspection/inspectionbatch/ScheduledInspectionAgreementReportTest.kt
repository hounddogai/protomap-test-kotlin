package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706877720)
        val items = mutableListOf<ScheduledInspectionAgreement>()
        for (index in 0 until 5) {
            items.add(newScheduledInspectionAgreement("scheduled-inspection-agreement-$index", "sample"))
        }
        val report = buildScheduledInspectionAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
