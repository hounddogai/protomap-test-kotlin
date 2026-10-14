package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRelayBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRelayBundle("code-388", "label-712")
        val view = toGlobalRelayBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRelayBundleViews(mutableListOf<GlobalRelayBundle>(item, item)).size, "views")
    }
}
