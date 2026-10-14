package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualClearanceOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualClearanceOverride("code-566", "name-588")
        val view = toManualClearanceOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualClearanceOverrideViews(mutableListOf<ManualClearanceOverride>(item, item)).size, "views")
    }
}
