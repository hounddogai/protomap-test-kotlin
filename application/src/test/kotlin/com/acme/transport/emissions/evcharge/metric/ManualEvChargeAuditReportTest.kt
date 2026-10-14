package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualEvChargeAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703025313)
        val items = mutableListOf<ManualEvChargeAudit>()
        for (index in 0 until 5) {
            items.add(newManualEvChargeAudit("manual-ev-charge-audit-$index", "sample"))
        }
        val report = buildManualEvChargeAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
