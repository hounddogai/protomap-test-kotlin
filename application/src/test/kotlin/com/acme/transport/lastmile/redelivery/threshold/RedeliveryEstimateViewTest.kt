package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryEstimate("code-240", "name-48")
        val view = toRedeliveryEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryEstimateViews(mutableListOf<RedeliveryEstimate>(item, item)).size, "views")
    }
}
