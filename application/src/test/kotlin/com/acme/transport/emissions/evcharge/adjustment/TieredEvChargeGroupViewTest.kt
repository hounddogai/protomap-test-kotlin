package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredEvChargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredEvChargeGroup("code-671", "title-473")
        val view = toTieredEvChargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredEvChargeGroupViews(mutableListOf<TieredEvChargeGroup>(item, item)).size, "views")
    }
}
