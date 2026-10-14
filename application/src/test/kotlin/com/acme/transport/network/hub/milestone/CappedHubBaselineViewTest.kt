package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedHubBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedHubBaseline("code-324", "label-393")
        val view = toCappedHubBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedHubBaselineViews(mutableListOf<CappedHubBaseline>(item, item)).size, "views")
    }
}
