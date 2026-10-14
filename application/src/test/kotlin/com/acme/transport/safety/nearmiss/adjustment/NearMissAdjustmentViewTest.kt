package com.acme.transport.safety.nearmiss.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissAdjustment("code-100", "title-557")
        val view = toNearMissAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissAdjustmentViews(mutableListOf<NearMissAdjustment>(item, item)).size, "views")
    }
}
