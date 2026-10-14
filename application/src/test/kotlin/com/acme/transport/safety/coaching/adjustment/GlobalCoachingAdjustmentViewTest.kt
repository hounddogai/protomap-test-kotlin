package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCoachingAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCoachingAdjustment("code-611", "title-355")
        val view = toGlobalCoachingAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCoachingAdjustmentViews(mutableListOf<GlobalCoachingAdjustment>(item, item)).size, "views")
    }
}
