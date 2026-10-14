package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationRule("code-22", "name-943")
        val view = toDeclarationRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationRuleViews(mutableListOf<DeclarationRule>(item, item)).size, "views")
    }
}
