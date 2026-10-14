package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeVariance("code-617", "name-670")
        val view = toDisputeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeVarianceViews(mutableListOf<DisputeVariance>(item, item)).size, "views")
    }
}
