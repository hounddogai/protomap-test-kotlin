package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientHubBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientHubBaseline("code-415", "name-315")
        val view = toLenientHubBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientHubBaselineViews(mutableListOf<LenientHubBaseline>(item, item)).size, "views")
    }
}
