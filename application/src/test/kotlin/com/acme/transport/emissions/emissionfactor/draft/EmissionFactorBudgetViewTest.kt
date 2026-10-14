package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorBudget("code-340", "name-671")
        val view = toEmissionFactorBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorBudgetViews(mutableListOf<EmissionFactorBudget>(item, item)).size, "views")
    }
}
