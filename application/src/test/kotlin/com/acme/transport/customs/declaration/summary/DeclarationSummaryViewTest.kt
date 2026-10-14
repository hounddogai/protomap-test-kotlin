package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationSummary("code-772", "name-889")
        val view = toDeclarationSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationSummaryViews(mutableListOf<DeclarationSummary>(item, item)).size, "views")
    }
}
