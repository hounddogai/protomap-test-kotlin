package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientHubThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientHubThreshold("code-981", "name-185")
        val view = toLenientHubThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientHubThresholdViews(mutableListOf<LenientHubThreshold>(item, item)).size, "views")
    }
}
