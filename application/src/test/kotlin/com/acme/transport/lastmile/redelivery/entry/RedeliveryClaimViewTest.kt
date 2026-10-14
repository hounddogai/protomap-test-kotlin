package com.acme.transport.lastmile.redelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryClaim("code-77", "label-36")
        val view = toRedeliveryClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryClaimViews(mutableListOf<RedeliveryClaim>(item, item)).size, "views")
    }
}
