package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalEvChargePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalEvChargePlan("code-403", "name-510")
        val view = toGlobalEvChargePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalEvChargePlanViews(mutableListOf<GlobalEvChargePlan>(item, item)).size, "views")
    }
}
