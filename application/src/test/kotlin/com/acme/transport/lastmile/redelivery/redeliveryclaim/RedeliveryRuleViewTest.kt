package com.acme.transport.lastmile.redelivery.redeliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryRule("code-454", "title-267")
        val view = toRedeliveryRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryRuleViews(mutableListOf<RedeliveryRule>(item, item)).size, "views")
    }
}
