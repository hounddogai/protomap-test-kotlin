package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelAudit("code-20", "title-86")
        val view = toParcelAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelAuditViews(mutableListOf<ParcelAudit>(item, item)).size, "views")
    }
}
