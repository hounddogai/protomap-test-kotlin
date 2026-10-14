package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderDraft("code-859", "label-349")
        val view = toTenderDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderDraftViews(mutableListOf<TenderDraft>(item, item)).size, "views")
    }
}
