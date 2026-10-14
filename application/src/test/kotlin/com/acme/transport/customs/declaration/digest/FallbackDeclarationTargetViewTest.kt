package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDeclarationTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDeclarationTarget("code-779", "title-619")
        val view = toFallbackDeclarationTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDeclarationTargetViews(mutableListOf<FallbackDeclarationTarget>(item, item)).size, "views")
    }
}
