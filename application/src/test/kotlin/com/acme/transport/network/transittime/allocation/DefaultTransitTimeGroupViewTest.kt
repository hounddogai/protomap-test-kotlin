package com.acme.transport.network.transittime.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTransitTimeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTransitTimeGroup("code-327", "label-581")
        val view = toDefaultTransitTimeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTransitTimeGroupViews(mutableListOf<DefaultTransitTimeGroup>(item, item)).size, "views")
    }
}
