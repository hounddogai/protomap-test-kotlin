package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveHubMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveHubMilestone("code-670", "label-440")
        val view = toAdaptiveHubMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveHubMilestoneViews(mutableListOf<AdaptiveHubMilestone>(item, item)).size, "views")
    }
}
