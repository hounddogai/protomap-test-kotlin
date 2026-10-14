package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackBackhaulMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackBackhaulMilestone("code-870", "title-592")
        val view = toFallbackBackhaulMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackBackhaulMilestoneViews(mutableListOf<FallbackBackhaulMilestone>(item, item)).size, "views")
    }
}
