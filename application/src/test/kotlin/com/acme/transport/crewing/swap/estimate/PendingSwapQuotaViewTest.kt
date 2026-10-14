package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingSwapQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingSwapQuota("code-240", "label-445")
        val view = toPendingSwapQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingSwapQuotaViews(mutableListOf<PendingSwapQuota>(item, item)).size, "views")
    }
}
