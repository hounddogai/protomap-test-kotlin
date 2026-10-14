package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedNearMissQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedNearMissQuota("code-158", "name-244")
        val view = toWeightedNearMissQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedNearMissQuotaViews(mutableListOf<WeightedNearMissQuota>(item, item)).size, "views")
    }
}
