package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDeclarationTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDeclarationTarget("code-981", "title-181")
        val view = toSeasonalDeclarationTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDeclarationTargetViews(mutableListOf<SeasonalDeclarationTarget>(item, item)).size, "views")
    }
}
