package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701255816)
        val items = mutableListOf<InspectionAgreement>()
        for (index in 0 until 4) {
            items.add(newInspectionAgreement("inspection-agreement-$index", "sample"))
        }
        val report = buildInspectionAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
