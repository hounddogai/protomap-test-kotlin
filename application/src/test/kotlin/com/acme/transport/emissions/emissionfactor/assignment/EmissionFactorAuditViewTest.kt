package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorAudit("code-645", "name-595")
        val view = toEmissionFactorAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorAuditViews(mutableListOf<EmissionFactorAudit>(item, item)).size, "views")
    }
}
