package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDwellMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDwellMilestone("code-158", "label-754")
        val view = toLenientDwellMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDwellMilestoneViews(mutableListOf<LenientDwellMilestone>(item, item)).size, "views")
    }
}
