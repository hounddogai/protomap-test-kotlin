package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorPolicy("code-930", "label-361")
        val view = toEmissionFactorPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorPolicyViews(mutableListOf<EmissionFactorPolicy>(item, item)).size, "views")
    }
}
