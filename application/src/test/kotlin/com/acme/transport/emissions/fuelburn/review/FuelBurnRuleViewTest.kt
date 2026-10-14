package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnRule("code-380", "name-298")
        val view = toFuelBurnRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnRuleViews(mutableListOf<FuelBurnRule>(item, item)).size, "views")
    }
}
