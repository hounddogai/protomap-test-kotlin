package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDeclarationTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDeclarationTemplate("code-491", "label-287")
        val view = toRegionalDeclarationTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDeclarationTemplateViews(mutableListOf<RegionalDeclarationTemplate>(item, item)).size, "views")
    }
}
