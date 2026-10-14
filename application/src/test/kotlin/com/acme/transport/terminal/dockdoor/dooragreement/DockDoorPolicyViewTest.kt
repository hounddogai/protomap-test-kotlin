package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorPolicy("code-194", "name-200")
        val view = toDockDoorPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorPolicyViews(mutableListOf<DockDoorPolicy>(item, item)).size, "views")
    }
}
