package com.acme.transport.emissions.emissionfactor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictEmissionFactorAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictEmissionFactorAllocation("code-706", "title-969")
        val view = toStrictEmissionFactorAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictEmissionFactorAllocationViews(mutableListOf<StrictEmissionFactorAllocation>(item, item)).size, "views")
    }
}
