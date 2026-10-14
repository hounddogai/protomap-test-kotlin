package com.acme.transport.linehaul.tender.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTenderQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTenderQuota("code-120", "label-665")
        val view = toGlobalTenderQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTenderQuotaViews(mutableListOf<GlobalTenderQuota>(item, item)).size, "views")
    }
}
