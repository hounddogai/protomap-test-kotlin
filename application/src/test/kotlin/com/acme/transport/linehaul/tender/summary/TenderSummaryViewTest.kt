package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderSummary("code-499", "title-471")
        val view = toTenderSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderSummaryViews(mutableListOf<TenderSummary>(item, item)).size, "views")
    }
}
