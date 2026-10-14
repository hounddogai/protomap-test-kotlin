package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFuelBurnAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFuelBurnAssignment("code-822", "name-881")
        val view = toGlobalFuelBurnAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFuelBurnAssignmentViews(mutableListOf<GlobalFuelBurnAssignment>(item, item)).size, "views")
    }
}
