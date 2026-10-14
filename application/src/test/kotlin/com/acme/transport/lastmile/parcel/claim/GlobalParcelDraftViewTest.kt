package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalParcelDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalParcelDraft("code-406", "name-504")
        val view = toGlobalParcelDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalParcelDraftViews(mutableListOf<GlobalParcelDraft>(item, item)).size, "views")
    }
}
