package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayRequest("code-128", "title-70")
        val view = toRelayRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayRequestViews(mutableListOf<RelayRequest>(item, item)).size, "views")
    }
}
