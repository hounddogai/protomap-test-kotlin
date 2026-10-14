package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDeclarationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDeclarationTier("code-241", "title-954")
        val view = toSeasonalDeclarationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDeclarationTierViews(mutableListOf<SeasonalDeclarationTier>(item, item)).size, "views")
    }
}
