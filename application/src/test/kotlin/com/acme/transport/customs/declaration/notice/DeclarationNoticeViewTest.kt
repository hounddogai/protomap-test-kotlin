package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationNotice("code-895", "title-756")
        val view = toDeclarationNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationNoticeViews(mutableListOf<DeclarationNotice>(item, item)).size, "views")
    }
}
