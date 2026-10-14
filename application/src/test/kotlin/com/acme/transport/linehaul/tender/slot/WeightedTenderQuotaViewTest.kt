package com.acme.transport.linehaul.tender.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTenderQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTenderQuota("code-51", "title-48")
        val view = toWeightedTenderQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTenderQuotaViews(mutableListOf<WeightedTenderQuota>(item, item)).size, "views")
    }
}
