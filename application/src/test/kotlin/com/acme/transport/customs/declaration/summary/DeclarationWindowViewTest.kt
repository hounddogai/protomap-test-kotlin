package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationWindow("code-717", "name-910")
        val view = toDeclarationWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationWindowViews(mutableListOf<DeclarationWindow>(item, item)).size, "views")
    }
}
