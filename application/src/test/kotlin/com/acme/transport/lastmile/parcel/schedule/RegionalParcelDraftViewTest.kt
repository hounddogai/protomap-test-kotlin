package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalParcelDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalParcelDraft("code-96", "label-415")
        val view = toRegionalParcelDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalParcelDraftViews(mutableListOf<RegionalParcelDraft>(item, item)).size, "views")
    }
}
