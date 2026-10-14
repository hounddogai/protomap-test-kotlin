package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNearMissQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNearMissQuota("code-294", "label-612")
        val view = toAdaptiveNearMissQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNearMissQuotaViews(mutableListOf<AdaptiveNearMissQuota>(item, item)).size, "views")
    }
}
