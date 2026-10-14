package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveEvChargeQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveEvChargeQuota("code-949", "label-370")
        val view = toAdaptiveEvChargeQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveEvChargeQuotaViews(mutableListOf<AdaptiveEvChargeQuota>(item, item)).size, "views")
    }
}
