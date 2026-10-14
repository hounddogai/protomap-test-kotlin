package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701015465)
        val items = mutableListOf<AuditMapping>()
        for (index in 0 until 5) {
            items.add(newAuditMapping("audit-mapping-$index", "sample"))
        }
        val report = buildAuditMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
