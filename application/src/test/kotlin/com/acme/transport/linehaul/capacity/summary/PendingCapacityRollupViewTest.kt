package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCapacityRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCapacityRollup("code-650", "title-394")
        val view = toPendingCapacityRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCapacityRollupViews(mutableListOf<PendingCapacityRollup>(item, item)).size, "views")
    }
}
