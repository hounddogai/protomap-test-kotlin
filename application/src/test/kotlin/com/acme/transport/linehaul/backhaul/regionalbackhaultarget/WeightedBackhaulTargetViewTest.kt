package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedBackhaulTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedBackhaulTarget("code-726", "name-161")
        val view = toWeightedBackhaulTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedBackhaulTargetViews(mutableListOf<WeightedBackhaulTarget>(item, item)).size, "views")
    }
}
