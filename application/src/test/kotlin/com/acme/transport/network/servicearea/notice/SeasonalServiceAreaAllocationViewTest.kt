package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalServiceAreaAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalServiceAreaAllocation("code-164", "title-600")
        val view = toSeasonalServiceAreaAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalServiceAreaAllocationViews(mutableListOf<SeasonalServiceAreaAllocation>(item, item)).size, "views")
    }
}
