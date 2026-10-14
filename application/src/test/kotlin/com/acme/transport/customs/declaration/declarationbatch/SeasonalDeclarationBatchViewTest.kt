package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDeclarationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDeclarationBatch("code-315", "title-684")
        val view = toSeasonalDeclarationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDeclarationBatchViews(mutableListOf<SeasonalDeclarationBatch>(item, item)).size, "views")
    }
}
