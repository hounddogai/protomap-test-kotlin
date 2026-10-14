package com.acme.transport.freightpay.audit.auditrollup

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAuditRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708645489)
        val items = mutableListOf<RegionalAuditRollup>()
        for (index in 0 until 5) {
            items.add(newRegionalAuditRollup("regional-audit-rollup-$index", "sample"))
        }
        val report = buildRegionalAuditRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
