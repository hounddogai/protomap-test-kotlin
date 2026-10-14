package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTireTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTireTarget("code-513", "label-327")
        val view = toPendingTireTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTireTargetViews(mutableListOf<PendingTireTarget>(item, item)).size, "views")
    }
}
