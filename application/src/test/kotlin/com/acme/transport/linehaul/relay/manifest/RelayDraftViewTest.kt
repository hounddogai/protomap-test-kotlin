package com.acme.transport.linehaul.relay.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayDraft("code-460", "title-763")
        val view = toRelayDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayDraftViews(mutableListOf<RelayDraft>(item, item)).size, "views")
    }
}
