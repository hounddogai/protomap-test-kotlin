package com.acme.transport.customs.clearance.seasonalclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalClearanceOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalClearanceOverride("code-278", "title-430")
        val view = toGlobalClearanceOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalClearanceOverrideViews(mutableListOf<GlobalClearanceOverride>(item, item)).size, "views")
    }
}
