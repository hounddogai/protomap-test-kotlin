package com.acme.transport.crewing.swap.swapmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedSwapMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedSwapMapping("code-542", "label-333")
        val view = toWeightedSwapMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedSwapMappingViews(mutableListOf<WeightedSwapMapping>(item, item)).size, "views")
    }
}
