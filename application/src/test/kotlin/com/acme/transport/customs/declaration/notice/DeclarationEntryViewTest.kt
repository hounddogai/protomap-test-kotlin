package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationEntry("code-651", "title-85")
        val view = toDeclarationEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationEntryViews(mutableListOf<DeclarationEntry>(item, item)).size, "views")
    }
}
