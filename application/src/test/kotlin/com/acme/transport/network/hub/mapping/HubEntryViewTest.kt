package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class HubEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubEntry("code-420", "name-456")
        val view = toHubEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubEntryViews(mutableListOf<HubEntry>(item, item)).size, "views")
    }
}
