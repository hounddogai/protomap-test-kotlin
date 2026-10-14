package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingNearMissAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingNearMissAdjustment("code-576", "label-762")
        val view = toRollingNearMissAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingNearMissAdjustmentViews(mutableListOf<RollingNearMissAdjustment>(item, item)).size, "views")
    }
}
