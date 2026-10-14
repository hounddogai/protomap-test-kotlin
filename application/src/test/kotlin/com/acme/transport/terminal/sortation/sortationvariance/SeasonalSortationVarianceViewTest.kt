package com.acme.transport.terminal.sortation.sortationvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalSortationVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalSortationVariance("code-737", "label-765")
        val view = toSeasonalSortationVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalSortationVarianceViews(mutableListOf<SeasonalSortationVariance>(item, item)).size, "views")
    }
}
