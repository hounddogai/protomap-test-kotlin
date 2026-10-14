package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTransitTimeRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTransitTimeRule("code-734", "label-526")
        val view = toBatchedTransitTimeRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTransitTimeRuleViews(mutableListOf<BatchedTransitTimeRule>(item, item)).size, "views")
    }
}
