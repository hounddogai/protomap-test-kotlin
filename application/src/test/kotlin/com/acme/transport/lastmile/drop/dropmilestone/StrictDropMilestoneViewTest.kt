package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDropMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDropMilestone("code-776", "name-332")
        val view = toStrictDropMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDropMilestoneViews(mutableListOf<StrictDropMilestone>(item, item)).size, "views")
    }
}
