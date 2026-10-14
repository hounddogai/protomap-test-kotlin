package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingNearMissSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingNearMissSlot("code-635", "name-609")
        val view = toPendingNearMissSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingNearMissSlotViews(mutableListOf<PendingNearMissSlot>(item, item)).size, "views")
    }
}
