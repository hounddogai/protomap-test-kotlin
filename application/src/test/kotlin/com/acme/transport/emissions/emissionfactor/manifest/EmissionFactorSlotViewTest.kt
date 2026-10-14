package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorSlot("code-609", "title-942")
        val view = toEmissionFactorSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorSlotViews(mutableListOf<EmissionFactorSlot>(item, item)).size, "views")
    }
}
