package com.acme.transport.terminal.crossdock.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockTier("code-511", "name-675")
        val view = toCrossDockTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockTierViews(mutableListOf<CrossDockTier>(item, item)).size, "views")
    }
}
