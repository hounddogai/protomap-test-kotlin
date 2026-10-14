package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAuditProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAuditProposal("code-854", "title-52")
        val view = toManualAuditProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAuditProposalViews(mutableListOf<ManualAuditProposal>(item, item)).size, "views")
    }
}
