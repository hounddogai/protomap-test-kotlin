package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRelayThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRelayThreshold("code-787", "name-321")
        val view = toPendingRelayThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRelayThresholdViews(mutableListOf<PendingRelayThreshold>(item, item)).size, "views")
    }
}
