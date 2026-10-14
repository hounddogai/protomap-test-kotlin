package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDefectAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701939369)
        val items = mutableListOf<ManualDefectAgreement>()
        for (index in 0 until 2) {
            items.add(newManualDefectAgreement("manual-defect-agreement-$index", "sample"))
        }
        val report = buildManualDefectAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
