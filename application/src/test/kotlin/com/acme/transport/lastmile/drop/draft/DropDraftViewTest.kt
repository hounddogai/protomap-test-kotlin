package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class DropDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropDraft("code-107", "label-407")
        val view = toDropDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropDraftViews(mutableListOf<DropDraft>(item, item)).size, "views")
    }
}
