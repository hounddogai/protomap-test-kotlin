package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualHubPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualHubPolicy("code-912", "name-606")
        val view = toManualHubPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualHubPolicyViews(mutableListOf<ManualHubPolicy>(item, item)).size, "views")
    }
}
