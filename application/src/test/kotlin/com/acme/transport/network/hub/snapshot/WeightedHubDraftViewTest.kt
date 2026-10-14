package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedHubDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedHubDraft("code-879", "label-237")
        val view = toWeightedHubDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedHubDraftViews(mutableListOf<WeightedHubDraft>(item, item)).size, "views")
    }
}
