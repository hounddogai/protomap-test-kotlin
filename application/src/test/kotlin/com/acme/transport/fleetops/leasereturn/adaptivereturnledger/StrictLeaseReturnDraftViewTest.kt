package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictLeaseReturnDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictLeaseReturnDraft("code-885", "label-813")
        val view = toStrictLeaseReturnDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictLeaseReturnDraftViews(mutableListOf<StrictLeaseReturnDraft>(item, item)).size, "views")
    }
}
