package com.acme.transport.network.node.nodeoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNodeWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNodeWindow("code-412", "label-246")
        val view = toAdaptiveNodeWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNodeWindowViews(mutableListOf<AdaptiveNodeWindow>(item, item)).size, "views")
    }
}
