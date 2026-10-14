package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictEvChargeSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictEvChargeSlot("code-336", "label-901")
        val view = toStrictEvChargeSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictEvChargeSlotViews(mutableListOf<StrictEvChargeSlot>(item, item)).size, "views")
    }
}
