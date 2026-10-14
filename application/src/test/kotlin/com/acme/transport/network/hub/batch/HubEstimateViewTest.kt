package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class HubEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubEstimate("code-28", "label-101")
        val view = toHubEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubEstimateViews(mutableListOf<HubEstimate>(item, item)).size, "views")
    }
}
