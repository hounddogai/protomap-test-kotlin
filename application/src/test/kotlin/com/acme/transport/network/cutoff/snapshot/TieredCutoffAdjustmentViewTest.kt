package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCutoffAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCutoffAdjustment("code-504", "label-687")
        val view = toTieredCutoffAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCutoffAdjustmentViews(mutableListOf<TieredCutoffAdjustment>(item, item)).size, "views")
    }
}
