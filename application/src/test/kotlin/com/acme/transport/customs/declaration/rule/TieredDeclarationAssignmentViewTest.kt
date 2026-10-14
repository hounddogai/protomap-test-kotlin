package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDeclarationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDeclarationAssignment("code-189", "label-887")
        val view = toTieredDeclarationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDeclarationAssignmentViews(mutableListOf<TieredDeclarationAssignment>(item, item)).size, "views")
    }
}
