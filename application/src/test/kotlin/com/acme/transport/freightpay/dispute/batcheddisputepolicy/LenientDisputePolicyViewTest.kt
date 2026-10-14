package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDisputePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDisputePolicy("code-949", "title-457")
        val view = toLenientDisputePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDisputePolicyViews(mutableListOf<LenientDisputePolicy>(item, item)).size, "views")
    }
}
