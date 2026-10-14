package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRedeliveryQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRedeliveryQuota("code-562", "title-18")
        val view = toStrictRedeliveryQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRedeliveryQuotaViews(mutableListOf<StrictRedeliveryQuota>(item, item)).size, "views")
    }
}
