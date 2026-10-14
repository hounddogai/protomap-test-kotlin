package com.acme.transport.customs.clearance.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceRollup("code-461", "name-357")
        val view = toClearanceRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceRollupViews(mutableListOf<ClearanceRollup>(item, item)).size, "views")
    }
}
