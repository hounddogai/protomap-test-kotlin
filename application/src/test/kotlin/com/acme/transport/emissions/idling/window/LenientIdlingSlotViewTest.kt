package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientIdlingSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientIdlingSlot("code-183", "label-371")
        val view = toLenientIdlingSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientIdlingSlotViews(mutableListOf<LenientIdlingSlot>(item, item)).size, "views")
    }
}
