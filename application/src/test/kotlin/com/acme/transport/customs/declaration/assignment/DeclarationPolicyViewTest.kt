package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationPolicy("code-961", "label-819")
        val view = toDeclarationPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationPolicyViews(mutableListOf<DeclarationPolicy>(item, item)).size, "views")
    }
}
