package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class HubVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubVariance("code-671", "name-666")
        val view = toHubVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubVarianceViews(mutableListOf<HubVariance>(item, item)).size, "views")
    }
}
