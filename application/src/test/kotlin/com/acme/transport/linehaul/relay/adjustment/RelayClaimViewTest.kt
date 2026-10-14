package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayClaim("code-383", "title-211")
        val view = toRelayClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayClaimViews(mutableListOf<RelayClaim>(item, item)).size, "views")
    }
}
