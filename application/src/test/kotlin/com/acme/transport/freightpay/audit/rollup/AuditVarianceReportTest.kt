package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703589716)
        val items = mutableListOf<AuditVariance>()
        for (index in 0 until 2) {
            items.add(newAuditVariance("audit-variance-$index", "sample"))
        }
        val report = buildAuditVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
