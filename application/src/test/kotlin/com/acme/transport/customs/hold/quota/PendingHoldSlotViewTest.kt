package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingHoldSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingHoldSlot("code-458", "title-800")
        val view = toPendingHoldSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingHoldSlotViews(mutableListOf<PendingHoldSlot>(item, item)).size, "views")
    }
}
