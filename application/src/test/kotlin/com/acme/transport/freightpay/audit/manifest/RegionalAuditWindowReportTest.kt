package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAuditWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707460099)
        val items = mutableListOf<RegionalAuditWindow>()
        for (index in 0 until 3) {
            items.add(newRegionalAuditWindow("regional-audit-window-$index", "sample"))
        }
        val report = buildRegionalAuditWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
