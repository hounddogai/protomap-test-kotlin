package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRelayPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRelayPolicy("code-662", "title-672")
        val view = toFallbackRelayPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRelayPolicyViews(mutableListOf<FallbackRelayPolicy>(item, item)).size, "views")
    }
}
