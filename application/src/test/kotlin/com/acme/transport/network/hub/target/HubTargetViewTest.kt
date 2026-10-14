package com.acme.transport.network.hub.target

import kotlin.test.Test
import kotlin.test.assertEquals

class HubTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubTarget("code-63", "title-550")
        val view = toHubTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubTargetViews(mutableListOf<HubTarget>(item, item)).size, "views")
    }
}
