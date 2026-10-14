package com.acme.transport.network.zone.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredZonePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredZonePlan("code-194", "title-481")
        val view = toTieredZonePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredZonePlanViews(mutableListOf<TieredZonePlan>(item, item)).size, "views")
    }
}
