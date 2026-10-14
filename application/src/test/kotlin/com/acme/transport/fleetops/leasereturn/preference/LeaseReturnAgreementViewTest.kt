package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnAgreement("code-530", "title-71")
        val view = toLeaseReturnAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnAgreementViews(mutableListOf<LeaseReturnAgreement>(item, item)).size, "views")
    }
}
