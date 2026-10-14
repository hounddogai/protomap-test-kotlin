package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCapacityGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCapacityGroup("code-985", "name-214")
        val view = toTieredCapacityGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCapacityGroupViews(mutableListOf<TieredCapacityGroup>(item, item)).size, "views")
    }
}
