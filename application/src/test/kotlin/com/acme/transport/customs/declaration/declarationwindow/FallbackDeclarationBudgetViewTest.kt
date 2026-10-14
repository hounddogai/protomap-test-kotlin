package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDeclarationBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDeclarationBudget("code-245", "name-652")
        val view = toFallbackDeclarationBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDeclarationBudgetViews(mutableListOf<FallbackDeclarationBudget>(item, item)).size, "views")
    }
}
