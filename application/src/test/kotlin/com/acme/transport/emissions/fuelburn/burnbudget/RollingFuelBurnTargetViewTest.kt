package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingFuelBurnTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingFuelBurnTarget("code-518", "title-608")
        val view = toRollingFuelBurnTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingFuelBurnTargetViews(mutableListOf<RollingFuelBurnTarget>(item, item)).size, "views")
    }
}
