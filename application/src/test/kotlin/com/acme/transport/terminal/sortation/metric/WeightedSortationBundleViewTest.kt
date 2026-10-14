package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedSortationBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedSortationBundle("code-22", "name-947")
        val view = toWeightedSortationBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedSortationBundleViews(mutableListOf<WeightedSortationBundle>(item, item)).size, "views")
    }
}
