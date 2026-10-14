package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class HubMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubMapping("code-448", "name-98")
        val view = toHubMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubMappingViews(mutableListOf<HubMapping>(item, item)).size, "views")
    }
}
