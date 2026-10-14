package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDeclarationSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDeclarationSlot("code-323", "label-822")
        val view = toRegionalDeclarationSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDeclarationSlotViews(mutableListOf<RegionalDeclarationSlot>(item, item)).size, "views")
    }
}
