package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCapacityRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCapacityRollup("code-839", "title-436")
        val view = toRollingCapacityRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCapacityRollupViews(mutableListOf<RollingCapacityRollup>(item, item)).size, "views")
    }
}
