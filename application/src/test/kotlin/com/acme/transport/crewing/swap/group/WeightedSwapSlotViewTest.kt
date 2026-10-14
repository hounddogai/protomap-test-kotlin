package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedSwapSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedSwapSlot("code-197", "name-482")
        val view = toWeightedSwapSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedSwapSlotViews(mutableListOf<WeightedSwapSlot>(item, item)).size, "views")
    }
}
