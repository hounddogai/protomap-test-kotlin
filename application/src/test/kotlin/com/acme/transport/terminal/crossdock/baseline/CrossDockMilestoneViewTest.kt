package com.acme.transport.terminal.crossdock.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockMilestone("code-917", "label-11")
        val view = toCrossDockMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockMilestoneViews(mutableListOf<CrossDockMilestone>(item, item)).size, "views")
    }
}
