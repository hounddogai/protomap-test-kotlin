package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingBackhaulMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingBackhaulMapping("code-491", "name-568")
        val view = toPendingBackhaulMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingBackhaulMappingViews(mutableListOf<PendingBackhaulMapping>(item, item)).size, "views")
    }
}
