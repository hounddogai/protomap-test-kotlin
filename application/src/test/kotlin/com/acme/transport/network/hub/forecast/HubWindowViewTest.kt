package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class HubWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubWindow("code-942", "name-706")
        val view = toHubWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubWindowViews(mutableListOf<HubWindow>(item, item)).size, "views")
    }
}
