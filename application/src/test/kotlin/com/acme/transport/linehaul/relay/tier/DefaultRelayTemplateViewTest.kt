package com.acme.transport.linehaul.relay.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRelayTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRelayTemplate("code-421", "label-662")
        val view = toDefaultRelayTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRelayTemplateViews(mutableListOf<DefaultRelayTemplate>(item, item)).size, "views")
    }
}
