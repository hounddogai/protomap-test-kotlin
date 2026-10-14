package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDisputeBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDisputeBundle("code-598", "label-578")
        val view = toScheduledDisputeBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDisputeBundleViews(mutableListOf<ScheduledDisputeBundle>(item, item)).size, "views")
    }
}
