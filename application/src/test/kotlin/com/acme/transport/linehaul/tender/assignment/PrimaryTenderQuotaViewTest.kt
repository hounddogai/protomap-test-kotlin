package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTenderQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTenderQuota("code-674", "title-807")
        val view = toPrimaryTenderQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTenderQuotaViews(mutableListOf<PrimaryTenderQuota>(item, item)).size, "views")
    }
}
