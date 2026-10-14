package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackNodeEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackNodeEstimate("code-611", "name-632")
        val view = toFallbackNodeEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackNodeEstimateViews(mutableListOf<FallbackNodeEstimate>(item, item)).size, "views")
    }
}
