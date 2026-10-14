package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayEntry("code-497", "label-955")
        val view = toRelayEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayEntryViews(mutableListOf<RelayEntry>(item, item)).size, "views")
    }
}
