package com.acme.transport.network.hub.hubestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultHubEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultHubEstimate("code-552", "label-828")
        val view = toDefaultHubEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultHubEstimateViews(mutableListOf<DefaultHubEstimate>(item, item)).size, "views")
    }
}
