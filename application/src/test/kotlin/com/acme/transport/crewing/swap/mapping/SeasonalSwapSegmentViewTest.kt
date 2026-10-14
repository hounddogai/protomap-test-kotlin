package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalSwapSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalSwapSegment("code-42", "name-43")
        val view = toSeasonalSwapSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalSwapSegmentViews(mutableListOf<SeasonalSwapSegment>(item, item)).size, "views")
    }
}
