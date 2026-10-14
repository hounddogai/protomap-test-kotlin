package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderQuota("code-277", "label-439")
        val view = toTenderQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderQuotaViews(mutableListOf<TenderQuota>(item, item)).size, "views")
    }
}
