package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDeclarationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDeclarationQuota("code-994", "label-527")
        val view = toRegionalDeclarationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDeclarationQuotaViews(mutableListOf<RegionalDeclarationQuota>(item, item)).size, "views")
    }
}
