package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceQuota("code-126", "title-320")
        val view = toClearanceQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceQuotaViews(mutableListOf<ClearanceQuota>(item, item)).size, "views")
    }
}
