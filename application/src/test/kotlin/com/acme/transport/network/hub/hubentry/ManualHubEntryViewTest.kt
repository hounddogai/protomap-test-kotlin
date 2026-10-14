package com.acme.transport.network.hub.hubentry

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualHubEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualHubEntry("code-696", "title-580")
        val view = toManualHubEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualHubEntryViews(mutableListOf<ManualHubEntry>(item, item)).size, "views")
    }
}
