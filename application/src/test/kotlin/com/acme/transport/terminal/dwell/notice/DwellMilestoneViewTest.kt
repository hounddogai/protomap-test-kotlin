package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellMilestone("code-455", "label-905")
        val view = toDwellMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellMilestoneViews(mutableListOf<DwellMilestone>(item, item)).size, "views")
    }
}
