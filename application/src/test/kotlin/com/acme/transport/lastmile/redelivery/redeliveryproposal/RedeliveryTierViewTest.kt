package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryTier("code-978", "title-879")
        val view = toRedeliveryTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryTierViews(mutableListOf<RedeliveryTier>(item, item)).size, "views")
    }
}
