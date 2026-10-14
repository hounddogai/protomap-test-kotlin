package com.acme.transport.lastmile.locker.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingLockerAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingLockerAgreement("code-531", "title-290")
        val view = toPendingLockerAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingLockerAgreementViews(mutableListOf<PendingLockerAgreement>(item, item)).size, "views")
    }
}
