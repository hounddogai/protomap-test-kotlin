package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedHubMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedHubMapping("code-49", "name-225")
        val view = toCappedHubMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedHubMappingViews(mutableListOf<CappedHubMapping>(item, item)).size, "views")
    }
}
