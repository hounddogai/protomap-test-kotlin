package com.acme.transport.customs.declaration.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDeclarationOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDeclarationOverride("code-904", "title-67")
        val view = toPrimaryDeclarationOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDeclarationOverrideViews(mutableListOf<PrimaryDeclarationOverride>(item, item)).size, "views")
    }
}
