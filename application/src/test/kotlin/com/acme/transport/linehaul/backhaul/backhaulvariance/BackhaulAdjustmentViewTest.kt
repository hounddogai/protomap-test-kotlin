package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulAdjustment("code-988", "name-173")
        val view = toBackhaulAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulAdjustmentViews(mutableListOf<BackhaulAdjustment>(item, item)).size, "views")
    }
}
