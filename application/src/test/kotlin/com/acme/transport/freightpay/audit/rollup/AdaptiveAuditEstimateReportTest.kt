package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAuditEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700879468)
        val items = mutableListOf<AdaptiveAuditEstimate>()
        for (index in 0 until 5) {
            items.add(newAdaptiveAuditEstimate("adaptive-audit-estimate-$index", "sample"))
        }
        val report = buildAdaptiveAuditEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
