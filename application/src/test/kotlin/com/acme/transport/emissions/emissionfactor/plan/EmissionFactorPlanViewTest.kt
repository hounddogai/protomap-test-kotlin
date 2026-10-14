package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorPlan("code-468", "name-288")
        val view = toEmissionFactorPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorPlanViews(mutableListOf<EmissionFactorPlan>(item, item)).size, "views")
    }
}
