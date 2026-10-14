package com.acme.transport.linehaul.relay.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingRelaySegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingRelaySegment("code-398", "name-135")
        val view = toPendingRelaySegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingRelaySegmentViews(mutableListOf<PendingRelaySegment>(item, item)).size, "views")
    }
}
