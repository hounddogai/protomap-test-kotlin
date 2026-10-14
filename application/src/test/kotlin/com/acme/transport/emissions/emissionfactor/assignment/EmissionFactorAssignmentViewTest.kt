package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorAssignment("code-245", "name-936")
        val view = toEmissionFactorAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorAssignmentViews(mutableListOf<EmissionFactorAssignment>(item, item)).size, "views")
    }
}
