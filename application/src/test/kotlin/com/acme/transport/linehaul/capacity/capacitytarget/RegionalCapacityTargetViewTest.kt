package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCapacityTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCapacityTarget("code-421", "name-721")
        val view = toRegionalCapacityTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCapacityTargetViews(mutableListOf<RegionalCapacityTarget>(item, item)).size, "views")
    }
}
