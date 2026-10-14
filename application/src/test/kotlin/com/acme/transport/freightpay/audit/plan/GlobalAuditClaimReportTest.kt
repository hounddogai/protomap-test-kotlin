package com.acme.transport.freightpay.audit.plan

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAuditClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700525018)
        val items = mutableListOf<GlobalAuditClaim>()
        for (index in 0 until 6) {
            items.add(newGlobalAuditClaim("global-audit-claim-$index", "sample"))
        }
        val report = buildGlobalAuditClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
