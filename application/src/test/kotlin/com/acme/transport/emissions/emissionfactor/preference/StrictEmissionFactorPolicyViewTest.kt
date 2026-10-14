package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictEmissionFactorPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictEmissionFactorPolicy("code-241", "name-222")
        val view = toStrictEmissionFactorPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictEmissionFactorPolicyViews(mutableListOf<StrictEmissionFactorPolicy>(item, item)).size, "views")
    }
}
