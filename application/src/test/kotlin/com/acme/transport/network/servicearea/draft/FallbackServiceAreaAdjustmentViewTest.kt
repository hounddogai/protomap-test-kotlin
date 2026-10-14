package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServiceAreaAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServiceAreaAdjustment("code-41", "name-216")
        val view = toFallbackServiceAreaAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServiceAreaAdjustmentViews(mutableListOf<FallbackServiceAreaAdjustment>(item, item)).size, "views")
    }
}
