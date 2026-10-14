package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeRule("code-552", "title-766")
        val view = toDisputeRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeRuleViews(mutableListOf<DisputeRule>(item, item)).size, "views")
    }
}
