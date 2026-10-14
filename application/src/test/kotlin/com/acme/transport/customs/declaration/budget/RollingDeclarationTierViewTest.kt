package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDeclarationTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDeclarationTier("code-63", "name-449")
        val view = toRollingDeclarationTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDeclarationTierViews(mutableListOf<RollingDeclarationTier>(item, item)).size, "views")
    }
}
