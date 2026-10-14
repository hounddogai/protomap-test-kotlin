package com.acme.transport.network.node.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNodeThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNodeThreshold("code-543", "name-342")
        val view = toAdaptiveNodeThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNodeThresholdViews(mutableListOf<AdaptiveNodeThreshold>(item, item)).size, "views")
    }
}
