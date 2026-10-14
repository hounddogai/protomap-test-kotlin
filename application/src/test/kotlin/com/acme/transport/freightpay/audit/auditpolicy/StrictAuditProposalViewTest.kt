package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAuditProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAuditProposal("code-884", "title-110")
        val view = toStrictAuditProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAuditProposalViews(mutableListOf<StrictAuditProposal>(item, item)).size, "views")
    }
}
