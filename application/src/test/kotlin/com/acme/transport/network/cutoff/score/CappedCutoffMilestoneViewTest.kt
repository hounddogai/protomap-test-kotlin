package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCutoffMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCutoffMilestone("code-941", "name-488")
        val view = toCappedCutoffMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCutoffMilestoneViews(mutableListOf<CappedCutoffMilestone>(item, item)).size, "views")
    }
}
