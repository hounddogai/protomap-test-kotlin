package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationQuota("code-493", "label-687")
        val view = toDeclarationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationQuotaViews(mutableListOf<DeclarationQuota>(item, item)).size, "views")
    }
}
