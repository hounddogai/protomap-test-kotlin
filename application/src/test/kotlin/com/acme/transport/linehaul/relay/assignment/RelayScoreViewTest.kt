package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayScore("code-434", "label-695")
        val view = toRelayScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayScoreViews(mutableListOf<RelayScore>(item, item)).size, "views")
    }
}
