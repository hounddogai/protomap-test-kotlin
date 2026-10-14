package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayThreshold("code-824", "title-287")
        val view = toRelayThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayThresholdViews(mutableListOf<RelayThreshold>(item, item)).size, "views")
    }
}
