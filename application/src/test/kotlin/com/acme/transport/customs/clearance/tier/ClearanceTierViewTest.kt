package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceTier("code-544", "label-106")
        val view = toClearanceTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceTierViews(mutableListOf<ClearanceTier>(item, item)).size, "views")
    }
}
