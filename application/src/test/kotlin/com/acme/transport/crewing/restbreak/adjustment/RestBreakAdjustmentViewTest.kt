package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakAdjustment("code-909", "label-695")
        val view = toRestBreakAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakAdjustmentViews(mutableListOf<RestBreakAdjustment>(item, item)).size, "views")
    }
}
