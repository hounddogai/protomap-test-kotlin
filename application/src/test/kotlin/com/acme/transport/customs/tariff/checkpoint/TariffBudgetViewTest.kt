package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffBudget("code-499", "name-705")
        val view = toTariffBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffBudgetViews(mutableListOf<TariffBudget>(item, item)).size, "views")
    }
}
