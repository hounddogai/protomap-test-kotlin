package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalNodeMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalNodeMapping("code-160", "name-371")
        val view = toRegionalNodeMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalNodeMappingViews(mutableListOf<RegionalNodeMapping>(item, item)).size, "views")
    }
}
