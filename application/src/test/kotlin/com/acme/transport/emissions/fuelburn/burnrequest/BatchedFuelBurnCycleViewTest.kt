package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedFuelBurnCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedFuelBurnCycle("code-15", "title-889")
        val view = toBatchedFuelBurnCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedFuelBurnCycleViews(mutableListOf<BatchedFuelBurnCycle>(item, item)).size, "views")
    }
}
