package com.acme.transport.network.hub.hubdraft

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledHubRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledHubRule("code-624", "name-275")
        val view = toScheduledHubRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledHubRuleViews(mutableListOf<ScheduledHubRule>(item, item)).size, "views")
    }
}
