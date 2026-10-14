package com.acme.transport.network.hub.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class HubDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubDraft("code-222", "name-120")
        val view = toHubDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubDraftViews(mutableListOf<HubDraft>(item, item)).size, "views")
    }
}
