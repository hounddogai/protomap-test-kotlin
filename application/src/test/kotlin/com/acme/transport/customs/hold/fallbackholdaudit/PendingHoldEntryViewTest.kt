package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingHoldEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingHoldEntry("code-343", "title-210")
        val view = toPendingHoldEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingHoldEntryViews(mutableListOf<PendingHoldEntry>(item, item)).size, "views")
    }
}
