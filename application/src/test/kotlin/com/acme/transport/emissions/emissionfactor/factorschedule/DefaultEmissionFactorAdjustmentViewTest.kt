package com.acme.transport.emissions.emissionfactor.factorschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEmissionFactorAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultEmissionFactorAdjustment("code-873", "name-977")
        val view = toDefaultEmissionFactorAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultEmissionFactorAdjustmentViews(mutableListOf<DefaultEmissionFactorAdjustment>(item, item)).size, "views")
    }
}
