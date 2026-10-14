package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceEntry("code-679", "name-126")
        val view = toClearanceEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceEntryViews(mutableListOf<ClearanceEntry>(item, item)).size, "views")
    }
}
