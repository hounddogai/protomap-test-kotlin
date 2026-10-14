package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalNearMissMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalNearMissMilestone("code-950", "name-632")
        val view = toRegionalNearMissMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalNearMissMilestoneViews(mutableListOf<RegionalNearMissMilestone>(item, item)).size, "views")
    }
}
