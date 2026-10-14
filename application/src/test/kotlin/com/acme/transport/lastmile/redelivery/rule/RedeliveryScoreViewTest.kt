package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryScore("code-922", "title-184")
        val view = toRedeliveryScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryScoreViews(mutableListOf<RedeliveryScore>(item, item)).size, "views")
    }
}
