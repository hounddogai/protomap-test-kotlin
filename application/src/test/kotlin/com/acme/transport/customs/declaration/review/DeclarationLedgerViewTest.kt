package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationLedger("code-328", "title-604")
        val view = toDeclarationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationLedgerViews(mutableListOf<DeclarationLedger>(item, item)).size, "views")
    }
}
