package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredRelayOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredRelayOverride("code-990", "name-694")
        val view = toTieredRelayOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredRelayOverrideViews(mutableListOf<TieredRelayOverride>(item, item)).size, "views")
    }
}
