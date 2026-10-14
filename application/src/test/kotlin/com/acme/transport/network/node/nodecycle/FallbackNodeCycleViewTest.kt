package com.acme.transport.network.node.nodecycle

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackNodeCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackNodeCycle("code-994", "name-653")
        val view = toFallbackNodeCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackNodeCycleViews(mutableListOf<FallbackNodeCycle>(item, item)).size, "views")
    }
}
