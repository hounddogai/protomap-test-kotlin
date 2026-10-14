package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class HubExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubException("code-426", "label-997")
        val view = toHubExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubExceptionViews(mutableListOf<HubException>(item, item)).size, "views")
    }
}
