package com.acme.transport.network.hub.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualHubBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualHubBaseline("code-767", "title-517")
        val view = toManualHubBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualHubBaselineViews(mutableListOf<ManualHubBaseline>(item, item)).size, "views")
    }
}
