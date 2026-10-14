package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayPolicy("code-991", "label-473")
        val view = toRelayPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayPolicyViews(mutableListOf<RelayPolicy>(item, item)).size, "views")
    }
}
