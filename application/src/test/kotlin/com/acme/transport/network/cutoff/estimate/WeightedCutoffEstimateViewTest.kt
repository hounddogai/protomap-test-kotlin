package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedCutoffEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedCutoffEstimate("code-981", "label-340")
        val view = toWeightedCutoffEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedCutoffEstimateViews(mutableListOf<WeightedCutoffEstimate>(item, item)).size, "views")
    }
}
