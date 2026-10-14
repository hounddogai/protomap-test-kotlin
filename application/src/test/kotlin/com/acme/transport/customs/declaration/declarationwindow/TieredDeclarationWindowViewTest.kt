package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDeclarationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDeclarationWindow("code-460", "title-77")
        val view = toTieredDeclarationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDeclarationWindowViews(mutableListOf<TieredDeclarationWindow>(item, item)).size, "views")
    }
}
