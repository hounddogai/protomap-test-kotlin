package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRelayDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRelayDigest("code-649", "label-627")
        val view = toAdaptiveRelayDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRelayDigestViews(mutableListOf<AdaptiveRelayDigest>(item, item)).size, "views")
    }
}
