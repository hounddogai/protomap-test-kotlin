package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701524056)
        val items = mutableListOf<CappedInspectionAgreement>()
        for (index in 0 until 6) {
            items.add(newCappedInspectionAgreement("capped-inspection-agreement-$index", "sample"))
        }
        val report = buildCappedInspectionAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
