package com.acme.transport.emissions.idling.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingSlot("code-805", "title-660")
        val view = toIdlingSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingSlotViews(mutableListOf<IdlingSlot>(item, item)).size, "views")
    }
}
