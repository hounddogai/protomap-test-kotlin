package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictViolationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictViolationQuota("code-265", "title-774")
        val view = toStrictViolationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictViolationQuotaViews(mutableListOf<StrictViolationQuota>(item, item)).size, "views")
    }
}
