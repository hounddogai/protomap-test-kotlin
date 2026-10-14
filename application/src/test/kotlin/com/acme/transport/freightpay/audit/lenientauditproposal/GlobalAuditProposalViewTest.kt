package com.acme.transport.freightpay.audit.lenientauditproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAuditProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAuditProposal("code-434", "title-543")
        val view = toGlobalAuditProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAuditProposalViews(mutableListOf<GlobalAuditProposal>(item, item)).size, "views")
    }
}
