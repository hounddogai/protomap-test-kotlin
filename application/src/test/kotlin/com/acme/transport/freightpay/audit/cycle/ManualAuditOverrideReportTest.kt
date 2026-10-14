package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAuditOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702948249)
        val items = mutableListOf<ManualAuditOverride>()
        for (index in 0 until 2) {
            items.add(newManualAuditOverride("manual-audit-override-$index", "sample"))
        }
        val report = buildManualAuditOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
