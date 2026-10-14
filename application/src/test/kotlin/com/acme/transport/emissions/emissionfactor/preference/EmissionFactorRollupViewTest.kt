package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorRollup("code-476", "title-232")
        val view = toEmissionFactorRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorRollupViews(mutableListOf<EmissionFactorRollup>(item, item)).size, "views")
    }
}
