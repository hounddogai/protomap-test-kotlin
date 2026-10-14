package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationQuota("code-291", "title-889")
        val view = toViolationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationQuotaViews(mutableListOf<ViolationQuota>(item, item)).size, "views")
    }
}
