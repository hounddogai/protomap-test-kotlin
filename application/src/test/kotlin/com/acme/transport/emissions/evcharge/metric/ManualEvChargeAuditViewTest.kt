package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualEvChargeAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualEvChargeAudit("code-491", "title-886")
        val view = toManualEvChargeAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualEvChargeAuditViews(mutableListOf<ManualEvChargeAudit>(item, item)).size, "views")
    }
}
