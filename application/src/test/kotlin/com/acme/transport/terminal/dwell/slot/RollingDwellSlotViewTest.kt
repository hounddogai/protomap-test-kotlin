package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDwellSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDwellSlot("code-555", "title-191")
        val view = toRollingDwellSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDwellSlotViews(mutableListOf<RollingDwellSlot>(item, item)).size, "views")
    }
}
