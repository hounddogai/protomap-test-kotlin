package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAuditOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAuditOverride("code-647", "name-137")
        val view = toManualAuditOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAuditOverrideViews(mutableListOf<ManualAuditOverride>(item, item)).size, "views")
    }
}
