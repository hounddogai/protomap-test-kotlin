package com.acme.transport.customs.tariff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTariffAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTariffAudit("code-882", "label-37")
        val view = toWeightedTariffAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTariffAuditViews(mutableListOf<WeightedTariffAudit>(item, item)).size, "views")
    }
}
