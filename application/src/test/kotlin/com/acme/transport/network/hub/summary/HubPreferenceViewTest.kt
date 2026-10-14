package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class HubPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubPreference("code-586", "name-218")
        val view = toHubPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubPreferenceViews(mutableListOf<HubPreference>(item, item)).size, "views")
    }
}
