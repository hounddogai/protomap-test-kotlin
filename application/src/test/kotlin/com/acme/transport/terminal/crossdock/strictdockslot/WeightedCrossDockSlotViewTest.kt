package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedCrossDockSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedCrossDockSlot("code-854", "name-938")
        val view = toWeightedCrossDockSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedCrossDockSlotViews(mutableListOf<WeightedCrossDockSlot>(item, item)).size, "views")
    }
}
