package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRelayWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRelayWindow("code-241", "label-556")
        val view = toStrictRelayWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRelayWindowViews(mutableListOf<StrictRelayWindow>(item, item)).size, "views")
    }
}
