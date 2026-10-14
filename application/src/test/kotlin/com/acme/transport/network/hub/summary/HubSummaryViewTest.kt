package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class HubSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubSummary("code-996", "label-836")
        val view = toHubSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubSummaryViews(mutableListOf<HubSummary>(item, item)).size, "views")
    }
}
