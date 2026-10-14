package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDeclarationAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDeclarationAdjustment("code-662", "name-600")
        val view = toFallbackDeclarationAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDeclarationAdjustmentViews(mutableListOf<FallbackDeclarationAdjustment>(item, item)).size, "views")
    }
}
