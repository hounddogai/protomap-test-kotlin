package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEvChargeQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEvChargeQuota("code-97", "label-474")
        val view = toWeightedEvChargeQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEvChargeQuotaViews(mutableListOf<WeightedEvChargeQuota>(item, item)).size, "views")
    }
}
