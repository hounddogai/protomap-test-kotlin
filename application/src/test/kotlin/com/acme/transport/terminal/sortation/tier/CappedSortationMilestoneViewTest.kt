package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedSortationMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedSortationMilestone("code-140", "label-421")
        val view = toCappedSortationMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedSortationMilestoneViews(mutableListOf<CappedSortationMilestone>(item, item)).size, "views")
    }
}
