package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTireBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTireBaseline("code-475", "label-671")
        val view = toPendingTireBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTireBaselineViews(mutableListOf<PendingTireBaseline>(item, item)).size, "views")
    }
}
