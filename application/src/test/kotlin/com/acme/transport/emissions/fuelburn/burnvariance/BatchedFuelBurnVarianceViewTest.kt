package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedFuelBurnVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedFuelBurnVariance("code-81", "label-105")
        val view = toBatchedFuelBurnVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedFuelBurnVarianceViews(mutableListOf<BatchedFuelBurnVariance>(item, item)).size, "views")
    }
}
