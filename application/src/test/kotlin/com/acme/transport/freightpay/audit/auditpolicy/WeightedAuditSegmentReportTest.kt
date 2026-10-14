package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAuditSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709324089)
        val items = mutableListOf<WeightedAuditSegment>()
        for (index in 0 until 3) {
            items.add(newWeightedAuditSegment("weighted-audit-segment-$index", "sample"))
        }
        val report = buildWeightedAuditSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
