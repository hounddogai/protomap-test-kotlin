package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveDockDoorCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveDockDoorCycle("code-975", "label-592")
        val view = toAdaptiveDockDoorCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveDockDoorCycleViews(mutableListOf<AdaptiveDockDoorCycle>(item, item)).size, "views")
    }
}
