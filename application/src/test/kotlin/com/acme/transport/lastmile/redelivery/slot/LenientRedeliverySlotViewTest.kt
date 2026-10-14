package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRedeliverySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRedeliverySlot("code-413", "name-317")
        val view = toLenientRedeliverySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRedeliverySlotViews(mutableListOf<LenientRedeliverySlot>(item, item)).size, "views")
    }
}
