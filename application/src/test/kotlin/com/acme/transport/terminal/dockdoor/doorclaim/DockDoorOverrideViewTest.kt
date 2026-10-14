package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorOverride("code-182", "label-839")
        val view = toDockDoorOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorOverrideViews(mutableListOf<DockDoorOverride>(item, item)).size, "views")
    }
}
