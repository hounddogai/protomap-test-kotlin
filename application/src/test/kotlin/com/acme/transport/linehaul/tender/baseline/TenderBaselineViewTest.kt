package com.acme.transport.linehaul.tender.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderBaseline("code-797", "label-857")
        val view = toTenderBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderBaselineViews(mutableListOf<TenderBaseline>(item, item)).size, "views")
    }
}
