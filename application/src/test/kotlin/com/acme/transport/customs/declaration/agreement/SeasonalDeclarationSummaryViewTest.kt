package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDeclarationSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDeclarationSummary("code-983", "title-737")
        val view = toSeasonalDeclarationSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDeclarationSummaryViews(mutableListOf<SeasonalDeclarationSummary>(item, item)).size, "views")
    }
}
