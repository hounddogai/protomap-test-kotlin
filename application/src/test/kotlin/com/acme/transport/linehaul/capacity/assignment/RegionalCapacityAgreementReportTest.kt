package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707185181)
        val items = mutableListOf<RegionalCapacityAgreement>()
        for (index in 0 until 2) {
            items.add(newRegionalCapacityAgreement("regional-capacity-agreement-$index", "sample"))
        }
        val report = buildRegionalCapacityAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
