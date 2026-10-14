package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRelayRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRelayRollup("code-175", "title-503")
        val view = toPendingRelayRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRelayRollupViews(mutableListOf<PendingRelayRollup>(item, item)).size, "views")
    }
}
