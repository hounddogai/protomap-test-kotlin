package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRelayQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRelayQuota("code-125", "title-791")
        val view = toManualRelayQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRelayQuotaViews(mutableListOf<ManualRelayQuota>(item, item)).size, "views")
    }
}
