package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDeclarationRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDeclarationRecord("code-91", "label-41")
        val view = toDefaultDeclarationRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDeclarationRecordViews(mutableListOf<DefaultDeclarationRecord>(item, item)).size, "views")
    }
}
