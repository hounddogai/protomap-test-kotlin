package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFuelBurnAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFuelBurnAssignment("code-441", "title-505")
        val view = toLenientFuelBurnAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFuelBurnAssignmentViews(mutableListOf<LenientFuelBurnAssignment>(item, item)).size, "views")
    }
}
