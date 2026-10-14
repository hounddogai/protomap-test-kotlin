package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerEstimate("code-40", "label-515")
        val view = toLockerEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerEstimateViews(mutableListOf<LockerEstimate>(item, item)).size, "views")
    }
}
