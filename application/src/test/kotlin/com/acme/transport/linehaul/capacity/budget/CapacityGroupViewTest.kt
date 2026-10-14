package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityGroup("code-535", "label-196")
        val view = toCapacityGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityGroupViews(mutableListOf<CapacityGroup>(item, item)).size, "views")
    }
}
