package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class HubPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubPolicy("code-409", "label-742")
        val view = toHubPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubPolicyViews(mutableListOf<HubPolicy>(item, item)).size, "views")
    }
}
