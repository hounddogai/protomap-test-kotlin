package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldQuota("code-858", "name-787")
        val view = toHoldQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldQuotaViews(mutableListOf<HoldQuota>(item, item)).size, "views")
    }
}
