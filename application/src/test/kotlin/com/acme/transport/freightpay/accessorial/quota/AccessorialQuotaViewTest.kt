package com.acme.transport.freightpay.accessorial.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialQuota("code-66", "title-581")
        val view = toAccessorialQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialQuotaViews(mutableListOf<AccessorialQuota>(item, item)).size, "views")
    }
}
