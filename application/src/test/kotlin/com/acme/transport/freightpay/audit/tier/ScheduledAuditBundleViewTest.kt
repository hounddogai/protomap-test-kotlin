package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAuditBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAuditBundle("code-268", "name-981")
        val view = toScheduledAuditBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAuditBundleViews(mutableListOf<ScheduledAuditBundle>(item, item)).size, "views")
    }
}
