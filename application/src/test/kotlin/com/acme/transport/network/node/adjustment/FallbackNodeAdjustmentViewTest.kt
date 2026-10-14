package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackNodeAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackNodeAdjustment("code-970", "label-455")
        val view = toFallbackNodeAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackNodeAdjustmentViews(mutableListOf<FallbackNodeAdjustment>(item, item)).size, "views")
    }
}
