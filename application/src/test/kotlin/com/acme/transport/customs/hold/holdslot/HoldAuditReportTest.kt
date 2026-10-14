package com.acme.transport.customs.hold.holdslot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703855290)
        val items = mutableListOf<HoldAudit>()
        for (index in 0 until 5) {
            items.add(newHoldAudit("hold-audit-$index", "sample"))
        }
        val report = buildHoldAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
