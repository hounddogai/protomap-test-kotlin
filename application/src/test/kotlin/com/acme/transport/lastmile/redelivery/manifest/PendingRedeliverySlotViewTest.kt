package com.acme.transport.lastmile.redelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRedeliverySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRedeliverySlot("code-677", "name-929")
        val view = toPendingRedeliverySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRedeliverySlotViews(mutableListOf<PendingRedeliverySlot>(item, item)).size, "views")
    }
}
