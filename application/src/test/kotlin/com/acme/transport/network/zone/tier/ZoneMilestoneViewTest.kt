package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneMilestone("code-891", "title-166")
        val view = toZoneMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneMilestoneViews(mutableListOf<ZoneMilestone>(item, item)).size, "views")
    }
}
