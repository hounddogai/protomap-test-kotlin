package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredNearMissAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredNearMissAdjustment("code-993", "name-516")
        val view = toTieredNearMissAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredNearMissAdjustmentViews(mutableListOf<TieredNearMissAdjustment>(item, item)).size, "views")
    }
}
