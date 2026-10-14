package com.acme.transport.linehaul.relay.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayAudit("code-855", "name-553")
        val view = toRelayAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayAuditViews(mutableListOf<RelayAudit>(item, item)).size, "views")
    }
}
