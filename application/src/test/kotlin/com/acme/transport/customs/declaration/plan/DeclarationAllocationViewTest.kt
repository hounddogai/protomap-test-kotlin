package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationAllocation("code-952", "name-816")
        val view = toDeclarationAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationAllocationViews(mutableListOf<DeclarationAllocation>(item, item)).size, "views")
    }
}
