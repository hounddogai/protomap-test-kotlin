package com.acme.transport.network.hub.hubentry

import kotlin.test.Test
import kotlin.test.assertEquals

class HubBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubBundle("code-195", "label-917")
        val view = toHubBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubBundleViews(mutableListOf<HubBundle>(item, item)).size, "views")
    }
}
