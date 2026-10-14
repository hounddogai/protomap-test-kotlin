package com.acme.transport.network.hub.hubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class HubProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubProfile("code-24", "label-53")
        val view = toHubProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubProfileViews(mutableListOf<HubProfile>(item, item)).size, "views")
    }
}
