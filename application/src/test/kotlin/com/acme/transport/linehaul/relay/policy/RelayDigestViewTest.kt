package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayDigest("code-562", "name-61")
        val view = toRelayDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayDigestViews(mutableListOf<RelayDigest>(item, item)).size, "views")
    }
}
