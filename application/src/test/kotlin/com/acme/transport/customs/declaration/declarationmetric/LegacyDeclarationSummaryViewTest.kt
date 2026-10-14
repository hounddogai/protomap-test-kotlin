package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDeclarationSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDeclarationSummary("code-772", "name-469")
        val view = toLegacyDeclarationSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDeclarationSummaryViews(mutableListOf<LegacyDeclarationSummary>(item, item)).size, "views")
    }
}
