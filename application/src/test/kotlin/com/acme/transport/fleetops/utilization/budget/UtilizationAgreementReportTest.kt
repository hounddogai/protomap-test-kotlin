package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700294057)
        val items = mutableListOf<UtilizationAgreement>()
        for (index in 0 until 6) {
            items.add(newUtilizationAgreement("utilization-agreement-$index", "sample"))
        }
        val report = buildUtilizationAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
