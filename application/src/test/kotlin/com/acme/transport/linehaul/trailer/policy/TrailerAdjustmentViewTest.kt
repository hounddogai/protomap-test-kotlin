package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerAdjustment("code-451", "title-968")
        val view = toTrailerAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerAdjustmentViews(mutableListOf<TrailerAdjustment>(item, item)).size, "views")
    }
}
