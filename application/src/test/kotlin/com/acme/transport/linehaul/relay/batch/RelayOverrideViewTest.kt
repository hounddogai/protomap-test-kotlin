package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayOverride("code-527", "name-5")
        val view = toRelayOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayOverrideViews(mutableListOf<RelayOverride>(item, item)).size, "views")
    }
}
