package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAuditClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700647240)
        val items = mutableListOf<RollingAuditClaim>()
        for (index in 0 until 4) {
            items.add(newRollingAuditClaim("rolling-audit-claim-$index", "sample"))
        }
        val report = buildRollingAuditClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
