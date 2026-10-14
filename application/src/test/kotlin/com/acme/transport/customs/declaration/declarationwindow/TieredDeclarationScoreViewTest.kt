package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDeclarationScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDeclarationScore("code-650", "label-48")
        val view = toTieredDeclarationScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDeclarationScoreViews(mutableListOf<TieredDeclarationScore>(item, item)).size, "views")
    }
}
