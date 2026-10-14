package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAuditBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAuditBundle("code-108", "label-190")
        val view = toAdaptiveAuditBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAuditBundleViews(mutableListOf<AdaptiveAuditBundle>(item, item)).size, "views")
    }
}
