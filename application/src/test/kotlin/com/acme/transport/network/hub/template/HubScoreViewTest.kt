package com.acme.transport.network.hub.template

import kotlin.test.Test
import kotlin.test.assertEquals

class HubScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubScore("code-937", "label-336")
        val view = toHubScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubScoreViews(mutableListOf<HubScore>(item, item)).size, "views")
    }
}
