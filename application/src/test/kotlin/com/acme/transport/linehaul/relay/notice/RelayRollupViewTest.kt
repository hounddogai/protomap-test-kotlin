package com.acme.transport.linehaul.relay.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayRollup("code-877", "title-217")
        val view = toRelayRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayRollupViews(mutableListOf<RelayRollup>(item, item)).size, "views")
    }
}
