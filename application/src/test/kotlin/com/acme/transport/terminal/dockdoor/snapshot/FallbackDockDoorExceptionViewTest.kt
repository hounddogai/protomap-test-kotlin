package com.acme.transport.terminal.dockdoor.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDockDoorExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDockDoorException("code-680", "name-354")
        val view = toFallbackDockDoorExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDockDoorExceptionViews(mutableListOf<FallbackDockDoorException>(item, item)).size, "views")
    }
}
