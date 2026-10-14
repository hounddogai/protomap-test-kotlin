package com.acme.transport.network.hub.legacyhubplan

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientHubEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientHubEstimate("code-140", "name-608")
        val view = toLenientHubEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientHubEstimateViews(mutableListOf<LenientHubEstimate>(item, item)).size, "views")
    }
}
