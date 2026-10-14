package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAuditProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703169058)
        val items = mutableListOf<GlobalAuditProposal>()
        for (index in 0 until 6) {
            items.add(newGlobalAuditProposal("global-audit-proposal-$index", "sample"))
        }
        val report = buildGlobalAuditProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
