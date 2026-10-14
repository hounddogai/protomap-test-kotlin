package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRedeliveryRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRedeliveryRule("code-157", "name-569")
        val view = toScheduledRedeliveryRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRedeliveryRuleViews(mutableListOf<ScheduledRedeliveryRule>(item, item)).size, "views")
    }
}
