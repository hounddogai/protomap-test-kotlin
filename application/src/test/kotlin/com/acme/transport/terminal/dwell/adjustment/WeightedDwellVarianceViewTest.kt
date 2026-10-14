package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDwellVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDwellVariance("code-713", "title-921")
        val view = toWeightedDwellVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDwellVarianceViews(mutableListOf<WeightedDwellVariance>(item, item)).size, "views")
    }
}
