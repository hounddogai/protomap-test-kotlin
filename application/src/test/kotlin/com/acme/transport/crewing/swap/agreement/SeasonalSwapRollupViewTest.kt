package com.acme.transport.crewing.swap.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalSwapRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalSwapRollup("code-421", "title-388")
        val view = toSeasonalSwapRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalSwapRollupViews(mutableListOf<SeasonalSwapRollup>(item, item)).size, "views")
    }
}
