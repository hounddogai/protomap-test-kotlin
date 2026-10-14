package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalNodeScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalNodeScore("code-173", "name-727")
        val view = toRegionalNodeScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalNodeScoreViews(mutableListOf<RegionalNodeScore>(item, item)).size, "views")
    }
}
