package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDockDoorTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDockDoorTarget("code-384", "label-582")
        val view = toTieredDockDoorTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDockDoorTargetViews(mutableListOf<TieredDockDoorTarget>(item, item)).size, "views")
    }
}
