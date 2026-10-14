package com.acme.transport.terminal.crossdock.target

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockTarget("code-614", "name-882")
        val view = toCrossDockTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockTargetViews(mutableListOf<CrossDockTarget>(item, item)).size, "views")
    }
}
