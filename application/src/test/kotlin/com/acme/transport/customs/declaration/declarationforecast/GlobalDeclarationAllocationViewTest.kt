package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDeclarationAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDeclarationAllocation("code-23", "title-588")
        val view = toGlobalDeclarationAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDeclarationAllocationViews(mutableListOf<GlobalDeclarationAllocation>(item, item)).size, "views")
    }
}
