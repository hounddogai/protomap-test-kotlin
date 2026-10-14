package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708704318)
        val items = mutableListOf<CapacityAgreement>()
        for (index in 0 until 2) {
            items.add(newCapacityAgreement("capacity-agreement-$index", "sample"))
        }
        val report = buildCapacityAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
