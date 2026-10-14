package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialAudit("code-864", "label-944")
        val view = toAccessorialAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialAuditViews(mutableListOf<AccessorialAudit>(item, item)).size, "views")
    }
}
