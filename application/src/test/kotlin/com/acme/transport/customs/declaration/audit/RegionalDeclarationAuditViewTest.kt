package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDeclarationAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDeclarationAudit("code-15", "label-92")
        val view = toRegionalDeclarationAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDeclarationAuditViews(mutableListOf<RegionalDeclarationAudit>(item, item)).size, "views")
    }
}
