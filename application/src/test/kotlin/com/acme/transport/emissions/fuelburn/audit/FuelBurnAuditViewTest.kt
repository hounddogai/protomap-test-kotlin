package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnAudit("code-809", "label-939")
        val view = toFuelBurnAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnAuditViews(mutableListOf<FuelBurnAudit>(item, item)).size, "views")
    }
}
