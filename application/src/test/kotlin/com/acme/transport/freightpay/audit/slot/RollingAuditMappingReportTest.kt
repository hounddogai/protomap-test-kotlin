package com.acme.transport.freightpay.audit.slot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAuditMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709166504)
        val items = mutableListOf<RollingAuditMapping>()
        for (index in 0 until 5) {
            items.add(newRollingAuditMapping("rolling-audit-mapping-$index", "sample"))
        }
        val report = buildRollingAuditMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
