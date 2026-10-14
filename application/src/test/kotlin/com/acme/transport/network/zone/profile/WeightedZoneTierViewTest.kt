package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedZoneTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedZoneTier("code-452", "label-845")
        val view = toWeightedZoneTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedZoneTierViews(mutableListOf<WeightedZoneTier>(item, item)).size, "views")
    }
}
