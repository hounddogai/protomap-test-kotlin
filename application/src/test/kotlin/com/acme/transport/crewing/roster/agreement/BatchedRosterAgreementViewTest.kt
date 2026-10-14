package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRosterAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRosterAgreement("code-868", "title-218")
        val view = toBatchedRosterAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRosterAgreementViews(mutableListOf<BatchedRosterAgreement>(item, item)).size, "views")
    }
}
