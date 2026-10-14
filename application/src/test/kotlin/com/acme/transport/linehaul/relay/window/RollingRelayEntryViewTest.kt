package com.acme.transport.linehaul.relay.window

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRelayEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRelayEntry("code-60", "name-593")
        val view = toRollingRelayEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRelayEntryViews(mutableListOf<RollingRelayEntry>(item, item)).size, "views")
    }
}
