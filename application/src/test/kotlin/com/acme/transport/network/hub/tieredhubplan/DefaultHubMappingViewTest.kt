package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultHubMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultHubMapping("code-732", "title-840")
        val view = toDefaultHubMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultHubMappingViews(mutableListOf<DefaultHubMapping>(item, item)).size, "views")
    }
}
