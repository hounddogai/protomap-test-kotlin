package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseReturnDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLeaseReturnDraft("code-964", "name-333")
        val view = toLeaseReturnDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLeaseReturnDraftViews(mutableListOf<LeaseReturnDraft>(item, item)).size, "views")
    }
}
