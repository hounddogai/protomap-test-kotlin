package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalAccessorialAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalAccessorialAllocation("code-860", "label-217")
        val view = toSeasonalAccessorialAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalAccessorialAllocationViews(mutableListOf<SeasonalAccessorialAllocation>(item, item)).size, "views")
    }
}
