package com.acme.transport.network.hub.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class HubThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubThreshold("code-295", "name-52")
        val view = toHubThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubThresholdViews(mutableListOf<HubThreshold>(item, item)).size, "views")
    }
}
