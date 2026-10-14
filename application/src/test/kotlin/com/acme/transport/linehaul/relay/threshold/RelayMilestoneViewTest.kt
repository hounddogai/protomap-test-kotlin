package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayMilestone("code-212", "name-522")
        val view = toRelayMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayMilestoneViews(mutableListOf<RelayMilestone>(item, item)).size, "views")
    }
}
