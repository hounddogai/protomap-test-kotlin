package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class HubGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubGroup("code-665", "title-308")
        val view = toHubGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubGroupViews(mutableListOf<HubGroup>(item, item)).size, "views")
    }
}
