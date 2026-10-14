package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class TireRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireRollup("code-966", "label-745")
        val view = toTireRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireRollupViews(mutableListOf<TireRollup>(item, item)).size, "views")
    }
}
