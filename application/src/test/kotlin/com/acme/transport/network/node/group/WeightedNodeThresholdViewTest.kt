package com.acme.transport.network.node.group

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedNodeThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedNodeThreshold("code-951", "label-163")
        val view = toWeightedNodeThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedNodeThresholdViews(mutableListOf<WeightedNodeThreshold>(item, item)).size, "views")
    }
}
