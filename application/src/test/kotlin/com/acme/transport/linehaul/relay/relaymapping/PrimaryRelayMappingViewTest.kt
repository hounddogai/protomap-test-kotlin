package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRelayMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRelayMapping("code-926", "title-287")
        val view = toPrimaryRelayMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRelayMappingViews(mutableListOf<PrimaryRelayMapping>(item, item)).size, "views")
    }
}
