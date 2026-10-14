package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderMilestone("code-298", "label-34")
        val view = toTenderMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderMilestoneViews(mutableListOf<TenderMilestone>(item, item)).size, "views")
    }
}
