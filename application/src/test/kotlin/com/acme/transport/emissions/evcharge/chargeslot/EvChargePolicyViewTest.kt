package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargePolicy("code-632", "name-537")
        val view = toEvChargePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargePolicyViews(mutableListOf<EvChargePolicy>(item, item)).size, "views")
    }
}
