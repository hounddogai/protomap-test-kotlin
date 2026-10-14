package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedHubEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedHubEntry("code-506", "name-620")
        val view = toCappedHubEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedHubEntryViews(mutableListOf<CappedHubEntry>(item, item)).size, "views")
    }
}
