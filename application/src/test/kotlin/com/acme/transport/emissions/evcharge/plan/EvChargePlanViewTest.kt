package com.acme.transport.emissions.evcharge.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargePlan("code-515", "title-634")
        val view = toEvChargePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargePlanViews(mutableListOf<EvChargePlan>(item, item)).size, "views")
    }
}
