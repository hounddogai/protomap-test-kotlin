package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorOverride("code-259", "name-555")
        val view = toEmissionFactorOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorOverrideViews(mutableListOf<EmissionFactorOverride>(item, item)).size, "views")
    }
}
