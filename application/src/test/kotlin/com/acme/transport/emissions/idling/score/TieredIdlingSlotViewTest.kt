package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredIdlingSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredIdlingSlot("code-918", "title-584")
        val view = toTieredIdlingSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredIdlingSlotViews(mutableListOf<TieredIdlingSlot>(item, item)).size, "views")
    }
}
