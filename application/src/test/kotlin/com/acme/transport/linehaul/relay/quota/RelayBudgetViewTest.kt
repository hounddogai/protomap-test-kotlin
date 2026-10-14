package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class RelayBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelayBudget("code-809", "label-310")
        val view = toRelayBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelayBudgetViews(mutableListOf<RelayBudget>(item, item)).size, "views")
    }
}
