package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCutoffThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCutoffThreshold("code-769", "label-409")
        val view = toRegionalCutoffThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCutoffThresholdViews(mutableListOf<RegionalCutoffThreshold>(item, item)).size, "views")
    }
}
