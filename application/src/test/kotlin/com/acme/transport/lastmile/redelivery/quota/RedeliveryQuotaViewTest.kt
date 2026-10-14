package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryQuota("code-580", "label-228")
        val view = toRedeliveryQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryQuotaViews(mutableListOf<RedeliveryQuota>(item, item)).size, "views")
    }
}
