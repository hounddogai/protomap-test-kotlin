package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayMapping("code-781", "label-927")
        val view = toRelayMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayMappingViews(mutableListOf<RelayMapping>(item, item)).size, "views")
    }
}
