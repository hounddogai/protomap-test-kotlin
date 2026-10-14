package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceProfile("code-457", "label-199")
        val view = toClearanceProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceProfileViews(mutableListOf<ClearanceProfile>(item, item)).size, "views")
    }
}
