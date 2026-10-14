package com.acme.transport.emissions.fuelburn.burnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFuelBurnTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFuelBurnTemplate("code-584", "title-232")
        val view = toPendingFuelBurnTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFuelBurnTemplateViews(mutableListOf<PendingFuelBurnTemplate>(item, item)).size, "views")
    }
}
