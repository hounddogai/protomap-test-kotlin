package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliverySlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliverySlot("code-179", "title-241")
        val view = toSeasonalRedeliverySlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliverySlotViews(mutableListOf<SeasonalRedeliverySlot>(item, item)).size, "views")
    }
}
