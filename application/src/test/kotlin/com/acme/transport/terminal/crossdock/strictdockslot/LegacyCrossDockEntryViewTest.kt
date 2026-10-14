package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCrossDockEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCrossDockEntry("code-3", "label-566")
        val view = toLegacyCrossDockEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCrossDockEntryViews(mutableListOf<LegacyCrossDockEntry>(item, item)).size, "views")
    }
}
