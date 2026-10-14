package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class HubTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubTier("code-53", "title-773")
        val view = toHubTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubTierViews(mutableListOf<HubTier>(item, item)).size, "views")
    }
}
