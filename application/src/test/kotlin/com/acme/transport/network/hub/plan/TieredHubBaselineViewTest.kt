package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredHubBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredHubBaseline("code-730", "name-26")
        val view = toTieredHubBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredHubBaselineViews(mutableListOf<TieredHubBaseline>(item, item)).size, "views")
    }
}
